package com.leochat.features.chat.presentation.providers

import com.leochat.features.chat.domain.usecases.AddGroupMembersUseCase
import com.leochat.features.chat.domain.usecases.GetGroupMembersUseCase
import com.leochat.features.chat.domain.usecases.MergeConversationIntoCacheUseCase
import com.leochat.features.chat.domain.usecases.RemoveGroupMemberUseCase
import com.leochat.features.chat.domain.usecases.ResolveConversationUseCase
import com.leochat.features.chat.domain.usecases.SetConversationMutedUseCase
import com.leochat.features.chat.presentation.controllers.GroupDetailsController
import com.leochat.features.chat.presentation.controllers.NewConversationController
import com.leochat.features.chat.presentation.controllers.NewGroupController
import com.leochat.features.chat.presentation.controllers.SharedMediaController
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Contacts / new conversation, new group, group details, and shared media.
 * Depends on `chatModule` (ChatRepository, ChatUseCaseBundle, StartDirectMessageUseCase,
 * CreateGroupUseCase) and `usersModule` (SearchUsersUseCase).
 */
val contactsModule = module {
    factoryOf(::ResolveConversationUseCase)
    factoryOf(::MergeConversationIntoCacheUseCase)
    factoryOf(::GetGroupMembersUseCase)
    factoryOf(::AddGroupMembersUseCase)
    factoryOf(::RemoveGroupMemberUseCase)
    factory { SetConversationMutedUseCase(get()) }

    viewModelOf(::NewConversationController)
    viewModelOf(::NewGroupController)
    viewModelOf(::GroupDetailsController)
    viewModelOf(::SharedMediaController)
}
