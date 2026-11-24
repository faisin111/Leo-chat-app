package com.leochat.features.admin

import com.leochat.core.config.AppConfig
import com.leochat.core.constants.StorageKeys
import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.Either
import com.leochat.core.network.createHttpClient
import com.leochat.core.session.SessionEvents
import com.leochat.core.session.TokenStore
import com.leochat.core.storage.InMemorySecureStorage
import com.leochat.features.admin.data.datasources.AdminRemoteDataSourceImpl
import com.leochat.features.admin.data.models.AdminDtoMapper
import com.leochat.features.admin.data.repositories.AdminRepositoryImpl
import com.leochat.features.admin.domain.entities.AdminReportStatus
import com.leochat.features.admin.domain.entities.AdminUserRole
import com.leochat.features.admin.domain.entities.AdminUserStatus
import com.leochat.features.admin.domain.usecases.ResolveReportUseCase
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdminRepositoryImplTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val requests = mutableListOf<HttpRequestData>()
    private var forbidden = false

    private val repo = run {
        val engine = MockEngine { request ->
            requests += request
            if (forbidden) return@MockEngine respondError(HttpStatusCode.Forbidden, """{"message":"Admins only"}""", json)
            val path = request.url.encodedPath
            when {
                path.endsWith("/admin/stats/overview") ->
                    respond("""{"totalUsers":120,"totalConversations":45.0,"totalReports":3}""", headers = json)
                path.endsWith("/admin/users") -> respond(
                    """{"items":[{"id":"u1","username":"alex","email":"a@x.io","displayName":"  Alex  ","role":"ROLE_ADMIN","status":"BANNED","createdAt":"2026-01-02T03:04:05Z"},{"id":7,"username":"sam"}],"nextCursor":null}""",
                    headers = json,
                )
                path.endsWith("/admin/reports") && request.method == HttpMethod.Get -> respond(
                    """{"items":[{"id":"r1","reporterId":"u2","targetType":"MESSAGE","targetId":"m9","reason":"SPAM","details":"ads","status":"RESOLVED","createdAt":"2026-02-01T00:00:00Z","resolution":"Warned"},{"id":"r2"}]}""",
                    headers = json,
                )
                else -> respond("", HttpStatusCode.NoContent)
            }
        }
        val secure = InMemorySecureStorage(mapOf(StorageKeys.accessToken to "t"))
        val http = createHttpClient(engine, AppConfig("https://api.test/api/v1", "wss://api.test/ws"), TokenStore(secure), SessionEvents())
        AdminRepositoryImpl(AdminRemoteDataSourceImpl(http))
    }

    private fun HttpRequestData.bodyJson(): JsonObject {
        val content = body as OutgoingContent
        val text = (content as TextContent).text
        return Json.parseToJsonElement(text).jsonObject
    }

    @Test
    fun statsParseNumbers() = runTest {
        val stats = repo.getStatsOverview().getOrNull()!!
        assertEquals(120, stats.totalUsers)
        assertEquals(45, stats.totalConversations)
        assertEquals(3, stats.totalReports)
        assertEquals("/api/v1/admin/stats/overview", requests.single().url.encodedPath)
    }

    @Test
    fun usersSendFiltersAndParseItems() = runTest {
        val list = repo.getUsers(query = "  al ", status = AdminUserStatus.BANNED).getOrNull()!!
        val params = requests.single().url.parameters
        assertEquals("50", params["limit"])
        assertEquals("al", params["q"])
        assertEquals("BANNED", params["status"])
        assertNull(params["cursor"])

        val alex = list[0]
        assertEquals("Alex", alex.effectiveName)
        assertTrue(alex.isAdmin)
        assertTrue(alex.isBanned)
        assertEquals(Instant.parse("2026-01-02T03:04:05Z"), alex.createdAt)

        val sam = list[1]
        assertEquals("7", sam.id)
        assertEquals("sam", sam.effectiveName)
        assertEquals("", sam.email)
        assertEquals(AdminUserRole.USER, sam.role)
        assertEquals(AdminUserStatus.ACTIVE, sam.status)
    }

    @Test
    fun allFilterAndBlankQueryAreOmitted() = runTest {
        repo.getUsers(query = "  ", status = AdminUserStatus.ALL, cursor = 40)
        val params = requests.single().url.parameters
        assertNull(params["q"])
        assertNull(params["status"])
        assertEquals("40", params["cursor"])

        repo.getReports(status = AdminReportStatus.ALL)
        assertNull(requests.last().url.parameters["status"])
    }

    @Test
    fun reportsParseWithDefaults() = runTest {
        val reports = repo.getReports(status = AdminReportStatus.OPEN).getOrNull()!!
        assertEquals("OPEN", requests.single().url.parameters["status"])
        val r1 = reports[0]
        assertTrue(r1.isResolved)
        assertTrue(r1.isMessageTarget)
        assertEquals("Warned", r1.resolution)
        assertEquals("ads", r1.details)
        val r2 = reports[1]
        assertEquals("MESSAGE", r2.targetType)
        assertEquals("OTHER", r2.reason)
        assertTrue(r2.isOpen)
        assertFalse(r2.hasDetails)
        assertNull(r2.createdAt)
    }

    @Test
    fun mutationsHitFlutterEndpoints() = runTest {
        assertTrue(repo.updateUserStatus("u1", AdminUserStatus.DISABLED, reason = "abuse").isRight)
        assertTrue(repo.forceLogoutUser("u1").isRight)
        assertTrue(repo.deleteUser("u1").isRight)
        assertTrue(repo.resolveReport("r1", "Warned").isRight)
        assertTrue(repo.deleteMessage("m9", "Deleted by moderator for SPAM").isRight)

        val (status, logout, delete, resolve, message) = requests
        assertEquals(HttpMethod.Patch, status.method)
        assertEquals("/api/v1/admin/users/u1/status", status.url.encodedPath)
        assertEquals("DISABLED", status.bodyJson()["status"]?.jsonPrimitive?.content)
        assertEquals("abuse", status.bodyJson()["reason"]?.jsonPrimitive?.content)

        assertEquals(HttpMethod.Post, logout.method)
        assertEquals("/api/v1/admin/users/u1/force-logout", logout.url.encodedPath)

        assertEquals(HttpMethod.Delete, delete.method)
        assertEquals("/api/v1/admin/users/u1", delete.url.encodedPath)

        assertEquals(HttpMethod.Patch, resolve.method)
        assertEquals("/api/v1/admin/reports/r1", resolve.url.encodedPath)
        assertEquals("Warned", resolve.bodyJson()["resolution"]?.jsonPrimitive?.content)
        assertNull(resolve.bodyJson()["action"])

        assertEquals(HttpMethod.Delete, message.method)
        assertEquals("/api/v1/admin/messages/m9", message.url.encodedPath)
        assertEquals("Deleted by moderator for SPAM", message.url.parameters["reason"])
    }

    @Test
    fun forbiddenMapsToForbiddenFailure() = runTest {
        forbidden = true
        val result = repo.getStatsOverview()
        assertIs<Either.Left<AppFailure>>(result)
        val failure = assertIs<AppFailure.Forbidden>(result.value)
        assertEquals("Admins only", failure.message)
    }

    @Test
    fun resolveUseCaseRejectsBlankNote() = runTest {
        val result = ResolveReportUseCase(repo).invoke("r1", "  ")
        assertIs<AppFailure.Validation>(result.leftOrNull())
        assertTrue(requests.isEmpty())
    }

    @Test
    fun mapperHandlesMissingItemsEnvelope() {
        assertTrue(AdminDtoMapper.users(JsonObject(emptyMap())).isEmpty())
        assertTrue(AdminDtoMapper.reports(JsonObject(emptyMap())).isEmpty())
        assertEquals(0, AdminDtoMapper.stats(JsonObject(emptyMap())).totalUsers)
    }
}
