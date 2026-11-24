package com.leochat.feature.chat.di

import com.leochat.feature.chat.domain.repository.ChatRepository
import com.leochat.feature.chat.domain.usecase.ChatUseCaseBundle
import com.leochat.feature.chat.domain.usecase.CreateGroupUseCase
import com.leochat.feature.chat.domain.usecase.StartDirectMessageUseCase
import com.leochat.feature.chat.domain.usecase.AddGroupMembersUseCase
import com.leochat.feature.chat.domain.usecase.GetGroupMembersUseCase
import com.leochat.feature.chat.domain.usecase.MergeConversationIntoCacheUseCase
import com.leochat.feature.chat.domain.usecase.RemoveGroupMemberUseCase
import com.leochat.feature.chat.domain.usecase.ResolveConversationUseCase
import com.leochat.feature.chat.domain.usecase.SetConversationMutedUseCase
import com.leochat.feature.chat.presentation.groupdetails.GroupDetailsViewModel
import com.leochat.feature.chat.presentation.newconversation.NewConversationViewModel
import com.leochat.feature.chat.presentation.newgroup.NewGroupViewModel
import com.leochat.feature.chat.presentation.sharedmedia.SharedMediaViewModel
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

    viewModelOf(::NewConversationViewModel)
    viewModelOf(::NewGroupViewModel)
    viewModelOf(::GroupDetailsViewModel)
    viewModelOf(::SharedMediaViewModel)
}
