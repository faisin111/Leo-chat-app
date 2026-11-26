/** The signed-in shell: tabs (chats, Leo AI, contacts, settings), rail/bottom bar, incoming-call banner. */
plugins {
    id("leochat.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:chat"))
            implementation(project(":feature:ai"))
            implementation(project(":feature:calls"))
            implementation(project(":feature:notifications"))
            implementation(project(":feature:settings"))
        }
    }
}
