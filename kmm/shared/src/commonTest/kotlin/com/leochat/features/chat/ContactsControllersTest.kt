package com.leochat.features.chat

import com.leochat.core.constants.ContactsStrings
import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.Either
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.domain.entities.ConversationMember
import com.leochat.features.chat.domain.entities.Message
import com.leochat.features.chat.domain.usecases.AddGroupMembersUseCase
import com.leochat.features.chat.domain.usecases.ChatUseCaseBundle
import com.leochat.features.chat.domain.usecases.CreateGroupUseCase
import com.leochat.features.chat.domain.usecases.GetGroupMembersUseCase
import com.leochat.features.chat.domain.usecases.MergeConversationIntoCacheUseCase
import com.leochat.features.chat.domain.usecases.RemoveGroupMemberUseCase
import com.leochat.features.chat.domain.usecases.ResolveConversationUseCase
import com.leochat.features.chat.domain.usecases.SetConversationMutedUseCase
import com.leochat.features.chat.domain.usecases.StartDirectMessageUseCase
import com.leochat.features.chat.presentation.controllers.GroupDetailsController
import com.leochat.features.chat.presentation.controllers.NewConversationController
import com.leochat.features.chat.presentation.controllers.NewGroupController
import com.leochat.features.chat.presentation.controllers.SharedMediaController
import com.leochat.features.chat.presentation.states.ContactsEffect
import com.leochat.features.chat.presentation.states.SharedMediaFilter
import com.leochat.features.users.domain.usecases.SearchUsersUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsControllersTest {
    private val repo = FakeChatRepository()
    private val users = ContactsFakeUsersRepository()

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.main() = Dispatchers.setMain(StandardTestDispatcher(testScheduler))

    private fun TestScope.collect(flow: Flow<ContactsEffect>): List<ContactsEffect> {
        val out = mutableListOf<ContactsEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.toList(out) }
        return out
    }

    // region NewConversationController

    private fun TestScope.newConversation(): NewConversationController {
        main()
        return NewConversationController(ChatUseCaseBundle.from(repo), StartDirectMessageUseCase(repo), MergeConversationIntoCacheUseCase(repo), SearchUsersUseCase(users))
    }

    @Test
    fun recentChatsShowCacheThenRefreshAndExcludeGroupsAndAi() = runTest {
        repo.cachedConversations = listOf(Conversation("cached", title = "Old"))
        repo.conversations = Either.Right(listOf(Conversation("c1", title = "Maya"), Conversation("g", type = "GROUP"), Conversation("ai", type = "AI")))
        val c = newConversation()
        advanceUntilIdle()
        assertFalse(c.state.value.isLoadingRecent)
        assertEquals(listOf("c1"), c.state.value.recentDirectChats.map { it.id })
    }

    @Test
    fun recentKeepsCacheWhenRefreshFails() = runTest {
        repo.cachedConversations = listOf(Conversation("cached", title = "Old"))
        repo.conversations = FakeChatRepository.offline
        val c = newConversation()
        advanceUntilIdle()
        assertEquals(listOf("cached"), c.state.value.recentDirectChats.map { it.id })
        assertFalse(c.state.value.isLoadingRecent)
    }

    @Test
    fun searchIsDebouncedAndBlankQueryClears() = runTest {
        val c = newConversation()
        advanceUntilIdle()
        c.search("ma")
        c.search("may ")
        assertTrue(c.state.value.search.isLoading)
        advanceUntilIdle()
        assertEquals(listOf("may"), users.queries)
        assertEquals(listOf(maya, sam), c.state.value.search.results)
        c.clearSearch()
        assertFalse(c.state.value.search.isActive)
        assertTrue(c.state.value.search.results.isEmpty())
    }

    @Test
    fun searchFailureIsExposed() = runTest {
        users.searchResult = Either.Left(AppFailure.Network("offline"))
        val c = newConversation()
        c.search("x")
        advanceUntilIdle()
        assertEquals("offline", c.state.value.search.errorMessage)
        assertFalse(c.state.value.search.isLoading)
    }

    @Test
    fun startChatOpensConversationAndMergesCache() = runTest {
        val c = newConversation()
        val effects = collect(c.effects)
        advanceUntilIdle()
        c.startChat(maya)
        assertTrue(c.state.value.isStartingChat)
        advanceUntilIdle()
        assertFalse(c.state.value.isStartingChat)
        assertEquals(listOf("u2"), repo.directTargets)
        assertEquals("dm", (effects.single() as ContactsEffect.ConversationOpened).conversation.id)
        assertEquals("dm", repo.cachedConversations.first().id)
    }

    @Test
    fun startChatFailureShowsError() = runTest {
        repo.directResult = Either.Left(AppFailure.Api("blocked"))
        val c = newConversation()
        val effects = collect(c.effects)
        c.startChat(maya)
        advanceUntilIdle()
        assertEquals(ContactsEffect.ShowError("blocked"), effects.single())
    }

    @Test
    fun startChatIgnoresSecondTapWhileRunning() = runTest {
        val c = newConversation()
        c.startChat(maya)
        c.startChat(sam)
        advanceUntilIdle()
        assertEquals(listOf("u2"), repo.directTargets)
    }

    @Test
    fun openExistingEmitsOpened() = runTest {
        val c = newConversation()
        val effects = collect(c.effects)
        c.openExisting(Conversation("c1"))
        advanceUntilIdle()
        assertEquals(ContactsEffect.ConversationOpened(Conversation("c1")), effects.single())
    }

    // endregion

    // region NewGroupController

    private fun TestScope.newGroup(): NewGroupController {
        main()
        return NewGroupController(CreateGroupUseCase(repo), MergeConversationIntoCacheUseCase(repo), SearchUsersUseCase(users))
    }

    @Test
    fun groupNeedsTitleAndMembers() = runTest {
        val c = newGroup()
        assertFalse(c.state.value.canCreate)
        c.setTitle("  ")
        c.toggle(maya)
        assertFalse(c.state.value.canCreate)
        c.setTitle("Team")
        assertTrue(c.state.value.canCreate)
        c.toggle(maya)
        assertFalse(c.state.value.canCreate)
        c.create()
        advanceUntilIdle()
        assertTrue(repo.createdGroups.isEmpty())
    }

    @Test
    fun toggleAddsAndRemovesById() = runTest {
        val c = newGroup()
        c.toggle(maya)
        c.toggle(sam)
        c.toggle(maya.copy(displayName = "changed"))
        assertEquals(listOf(sam), c.state.value.selected)
    }

    @Test
    fun createGroupSendsTrimmedTitleAndEmitsSuccessThenOpened() = runTest {
        val c = newGroup()
        val effects = collect(c.effects)
        c.setTitle(" Design ")
        c.toggle(maya)
        c.toggle(sam)
        c.create()
        assertTrue(c.state.value.isCreating)
        advanceUntilIdle()
        assertFalse(c.state.value.isCreating)
        assertEquals(listOf("Design" to listOf("u2", "u3")), repo.createdGroups)
        assertEquals(ContactsEffect.ShowSuccess(ContactsStrings.groupCreated("Design")), effects[0])
        assertEquals("g", (effects[1] as ContactsEffect.ConversationOpened).conversation.id)
        assertEquals("g", repo.cachedConversations.first().id)
    }

    @Test
    fun createGroupFailureShowsError() = runTest {
        repo.groupResult = Either.Left(AppFailure.Api("nope"))
        val c = newGroup()
        val effects = collect(c.effects)
        c.setTitle("T")
        c.toggle(maya)
        c.create()
        advanceUntilIdle()
        assertEquals(listOf<ContactsEffect>(ContactsEffect.ShowError("nope")), effects)
    }

    // endregion

    // region GroupDetailsController

    private val owner = ConversationMember("g1", "u1", "OWNER", username = "alex", displayName = "Alex")
    private val member = ConversationMember("g1", "u2", "MEMBER", username = "maya")
    private val otherOwner = ConversationMember("g1", "u9", "owner")

    private fun TestScope.groupDetails(): GroupDetailsController {
        main()
        return GroupDetailsController(
            ChatUseCaseBundle.from(repo),
            ResolveConversationUseCase(repo),
            GetGroupMembersUseCase(repo),
            AddGroupMembersUseCase(repo),
            RemoveGroupMemberUseCase(repo),
            SetConversationMutedUseCase(repo),
            MergeConversationIntoCacheUseCase(repo),
            SearchUsersUseCase(users),
        )
    }

    @Test
    fun loadsConversationFromCacheAndMembers() = runTest {
        repo.cachedConversations = listOf(Conversation("g1", type = "GROUP", title = "Team", isMuted = true, lastMessageSnippet = "hi"))
        repo.membersResult = Either.Right(listOf(owner, member))
        val c = groupDetails()
        c.load("g1")
        advanceUntilIdle()
        val s = c.state.value
        assertEquals("Team", s.title)
        assertEquals("hi", s.description)
        assertTrue(s.isMuted)
        assertEquals("u1", s.currentUserId)
        assertEquals(2, s.members.size)
        assertTrue(s.isOwnerOrAdmin)
        assertTrue(s.canRemove(member))
        assertFalse(s.canRemove(owner))
        assertFalse(s.isLoadingMembers)
    }

    @Test
    fun plainMemberCannotManageAndOwnersAreNeverRemovable() = runTest {
        repo.membersResult = Either.Right(listOf(owner.copy(userId = "u5"), member.copy(userId = "u1"), otherOwner))
        val c = groupDetails()
        c.load("g1")
        advanceUntilIdle()
        assertFalse(c.state.value.isOwnerOrAdmin)
        assertFalse(c.state.value.canRemove(otherOwner))
    }

    @Test
    fun memberLoadFailureKeepsEmptyListWithError() = runTest {
        repo.membersResult = Either.Left(AppFailure.Network("down"))
        val c = groupDetails()
        c.load("g1")
        advanceUntilIdle()
        assertTrue(c.state.value.members.isEmpty())
        assertEquals("down", c.state.value.membersErrorMessage)
        assertEquals("Conversation", c.state.value.title)
    }

    @Test
    fun toggleMuteMutesThenUnmutesAndUpdatesCache() = runTest {
        repo.cachedConversations = listOf(Conversation("g1", type = "GROUP", title = "Team"))
        val c = groupDetails()
        val effects = collect(c.effects)
        c.load("g1")
        advanceUntilIdle()
        c.toggleMute()
        advanceUntilIdle()
        assertTrue(c.state.value.isMuted)
        assertTrue(repo.cachedConversations.single().isMuted)
        c.toggleMute()
        advanceUntilIdle()
        assertFalse(c.state.value.isMuted)
        assertEquals(2, repo.mutes.size)
        assertTrue(repo.mutes[0].second!!.isNotEmpty())
        assertNull(repo.mutes[1].second)
        assertEquals(
            listOf<ContactsEffect>(ContactsEffect.ShowSuccess(ContactsStrings.notificationsMuted), ContactsEffect.ShowSuccess(ContactsStrings.notificationsUnmuted)),
            effects,
        )
    }

    @Test
    fun muteFailureKeepsState() = runTest {
        repo.muteResult = Either.Left(AppFailure.Api("x"))
        val c = groupDetails()
        val effects = collect(c.effects)
        c.load("g1")
        advanceUntilIdle()
        c.toggleMute()
        advanceUntilIdle()
        assertFalse(c.state.value.isMuted)
        assertEquals(ContactsEffect.ShowError("x"), effects.single())
    }

    @Test
    fun removeMemberDropsRow() = runTest {
        repo.membersResult = Either.Right(listOf(owner, member))
        val c = groupDetails()
        val effects = collect(c.effects)
        c.load("g1")
        advanceUntilIdle()
        c.removeMember(member)
        advanceUntilIdle()
        assertEquals(listOf("g1" to "u2"), repo.removedMembers)
        assertEquals(listOf(owner), c.state.value.members)
        assertEquals(ContactsEffect.ShowSuccess(ContactsStrings.removedFromGroup("maya")), effects.single())
    }

    @Test
    fun addMemberFlowFiltersExistingClosesSheetAndReloads() = runTest {
        repo.membersResult = Either.Right(listOf(owner, member))
        val c = groupDetails()
        val effects = collect(c.effects)
        c.load("g1")
        advanceUntilIdle()
        c.openAddSheet()
        assertTrue(c.state.value.isAddSheetOpen)
        c.searchToAdd("a")
        advanceUntilIdle()
        assertEquals(listOf(sam), c.state.value.eligibleToAdd)
        repo.membersResult = Either.Right(listOf(owner, member, ConversationMember("g1", "u3", "MEMBER", username = "sam")))
        c.addMember(sam)
        advanceUntilIdle()
        assertEquals(listOf("g1" to listOf("u3")), repo.addedMembers)
        assertFalse(c.state.value.isAddSheetOpen)
        assertFalse(c.state.value.addSearch.isActive)
        assertEquals(3, c.state.value.members.size)
        assertEquals(ContactsEffect.ShowSuccess(ContactsStrings.addedToGroup("sam")), effects.single())
    }

    @Test
    fun addMemberFailureKeepsSheetOpen() = runTest {
        repo.memberMutationResult = Either.Left(AppFailure.Forbidden("not admin"))
        val c = groupDetails()
        val effects = collect(c.effects)
        c.load("g1")
        advanceUntilIdle()
        c.openAddSheet()
        c.addMember(sam)
        advanceUntilIdle()
        assertTrue(c.state.value.isAddSheetOpen)
        assertFalse(c.state.value.isAddingMember)
        assertEquals(ContactsEffect.ShowError("not admin"), effects.single())
    }

    @Test
    fun actionsWithoutLoadedGroupAreIgnored() = runTest {
        val c = groupDetails()
        c.openAddSheet()
        c.toggleMute()
        c.removeMember(member)
        advanceUntilIdle()
        assertFalse(c.state.value.isAddSheetOpen)
        assertTrue(repo.mutes.isEmpty() && repo.removedMembers.isEmpty())
    }

    // endregion

    // region SharedMediaController

    private fun TestScope.sharedMedia(): SharedMediaController {
        main()
        return SharedMediaController(ChatUseCaseBundle.from(repo), ResolveConversationUseCase(repo))
    }

    private val image = Message("m1", "c1", type = "IMAGE", content = "https://x/a.png", seq = 1)
    private val file = Message("m2", "c1", type = "FILE", content = "https://x/docs/report.pdf", seq = 2)
    private val link = Message("m3", "c1", content = "see https://leo.chat now", seq = 3)

    @Test
    fun sharedMediaShowsCacheThenRefreshesAndClassifies() = runTest {
        repo.cachedMessages["c1"] = listOf(image)
        repo.messages = Either.Right(listOf(image, file, link, Message("m4", "c1", content = "plain", seq = 4)))
        val c = sharedMedia()
        c.load("c1")
        advanceUntilIdle()
        val s = c.state.value
        assertFalse(s.isLoading)
        assertEquals(listOf("m1"), s.buckets.media.map { it.id })
        assertEquals(listOf("m2"), s.buckets.files.map { it.id })
        assertEquals(listOf("m3"), s.buckets.links.map { it.id })
        c.setFilter(SharedMediaFilter.Links)
        assertEquals(SharedMediaFilter.Links, c.state.value.filter)
    }

    @Test
    fun sharedMediaKeepsCacheOnRefreshError() = runTest {
        repo.cachedMessages["c1"] = listOf(image)
        repo.messages = FakeChatRepository.offline
        val c = sharedMedia()
        c.load("c1")
        advanceUntilIdle()
        assertEquals(listOf(image), c.state.value.messages)
        assertEquals(AppFailure.Network().message, c.state.value.errorMessage)
        assertFalse(c.state.value.isLoading)
    }

    @Test
    fun sharedMediaTitleUsesConversation() = runTest {
        repo.conversationResult = { Either.Right(Conversation(it, targetDisplayName = "Maya")) }
        val c = sharedMedia()
        assertEquals(ContactsStrings.sharedMediaAndFiles, c.state.value.title)
        c.load("c1")
        advanceUntilIdle()
        assertEquals("Maya", c.state.value.title)
    }

    // endregion
}
