package com.leochat.core.constants

/** Strings for new conversation, new group, group details, and shared media screens. */
object ContactsStrings {
    // New conversation / Contacts tab
    const val newConversation = "New conversation"
    const val searchByNameOrUsername = "Search by name or username"
    const val noPeopleFound = "No people found"
    const val noPeopleFoundHint = "Check the spelling or try searching for another username."
    const val createAGroup = "Create a group"
    const val createAGroupSubtitle = "Bring your team or friends together"
    const val recentContacts = "RECENT CONTACTS"
    const val startNewConversation = "Start a new conversation"
    const val startNewConversationHint = "Search for people above by their username or display name to begin chatting."
    const val back = "Back"
    const val close = "Close"
    const val clearSearch = "Clear search"

    fun peopleCount(count: Int) = "$count people"
    fun searchFailed(reason: String) = "Could not load search results: $reason"
    fun handle(username: String) = "@$username"

    // New group
    const val newGroup = "New Group"
    const val groupDetailsLabel = "GROUP DETAILS"
    const val groupTitleHint = "e.g. Design Team or Weekend Trip"
    const val searchPeopleToAdd = "Search people to add..."
    const val searchUsersPrompt = "Search users by username or display name"
    const val selectAtLeastOneMember = "Select at least 1 member"
    const val removeMember = "Remove member"

    fun noUsersMatching(query: String) = "No users found matching \"$query\""
    fun groupSearchFailed(reason: String) = "Search failed: $reason"
    fun createGroupCount(count: Int) = "Create Group ($count)"
    fun groupCreated(title: String) = "Group \"$title\" created!"

    // Group details
    const val groupDetails = "Group details"
    const val mute = "Mute"
    const val muted = "Muted"
    const val media = "Media"
    const val add = "Add"
    const val sharedMediaAndFiles = "Shared media & files"
    const val noMembersFound = "No members found"
    const val owner = "Owner"
    const val admin = "Admin"
    const val remove = "Remove"
    const val notificationsMuted = "Notifications muted"
    const val notificationsUnmuted = "Notifications unmuted"
    const val addGroupMember = "Add group member"
    const val noMatchingUsersToAdd = "No matching users to add"
    const val membersLoading = "..."

    fun membersHeader(count: String) = "Members · $count"
    fun youSuffix(name: String) = "$name (You)"
    fun removedFromGroup(name: String) = "Removed $name from group"
    fun addedToGroup(name: String) = "Added $name to group"
    fun addSearchFailed(reason: String) = "Search error: $reason"

    // Shared media
    const val everythingInOnePlace = "Everything in one place"
    const val noMediaYet = "No media shared yet"
    const val noMediaYetHint = "Photos and videos sent in this chat will appear here."
    const val noFilesYet = "No files shared yet"
    const val noFilesYetHint = "Documents, audio, and attachments will appear here."
    const val noLinksYet = "No links shared yet"
    const val noLinksYetHint = "Web links and URLs sent in this chat will appear here."
    const val sharedFile = "Shared file"

    fun sharedIn(title: String) = "Shared in $title"
    fun mediaCount(count: Int) = "Media ($count)"
    fun filesCount(count: Int) = "Files ($count)"
    fun linksCount(count: Int) = "Links ($count)"
}
