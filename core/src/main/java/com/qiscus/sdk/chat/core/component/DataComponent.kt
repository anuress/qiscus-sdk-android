package com.qiscus.sdk.chat.core.component

import android.content.Context
import com.qiscus.sdk.chat.data.*
import com.qiscus.sdk.chat.data.local.*
import com.qiscus.sdk.chat.data.local.database.DbOpenHelper
import com.qiscus.sdk.chat.data.local.util.FileUtil
import com.qiscus.sdk.chat.data.local.util.MimeTypeGuesserImpl
import com.qiscus.sdk.chat.data.pubsub.comment.CommentPublisher
import com.qiscus.sdk.chat.data.pubsub.comment.CommentSubscriber
import com.qiscus.sdk.chat.data.pubsub.file.FilePublisher
import com.qiscus.sdk.chat.data.pubsub.file.FileSubscriber
import com.qiscus.sdk.chat.data.pubsub.room.RoomPublisher
import com.qiscus.sdk.chat.data.pubsub.room.RoomSubscriber
import com.qiscus.sdk.chat.data.pusher.CommentDataPusher
import com.qiscus.sdk.chat.data.pusher.FileDataPusher
import com.qiscus.sdk.chat.data.pusher.QiscusMqttClient
import com.qiscus.sdk.chat.data.pusher.RoomDataPusher
import com.qiscus.sdk.chat.data.pusher.mapper.CommentPayloadMapper
import com.qiscus.sdk.chat.data.remote.*
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.account.AccountRemote
import com.qiscus.sdk.chat.data.source.comment.CommentLocal
import com.qiscus.sdk.chat.data.source.comment.CommentRemote
import com.qiscus.sdk.chat.data.source.file.FileLocal
import com.qiscus.sdk.chat.data.source.file.FileRemote
import com.qiscus.sdk.chat.data.source.room.RoomLocal
import com.qiscus.sdk.chat.data.source.room.RoomRemote
import com.qiscus.sdk.chat.data.source.user.UserLocal
import com.qiscus.sdk.chat.data.util.*
import com.qiscus.sdk.chat.domain.pubsub.CommentObserver
import com.qiscus.sdk.chat.domain.pubsub.FileObserver
import com.qiscus.sdk.chat.domain.pubsub.QiscusPubSubClient
import com.qiscus.sdk.chat.domain.pubsub.RoomObserver
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import com.qiscus.sdk.chat.domain.repository.CommentRepository
import com.qiscus.sdk.chat.domain.repository.RoomRepository
import com.qiscus.sdk.chat.domain.repository.UserRepository
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class DataComponent
(
        private val context: Context,
        private val applicationWatcher: ApplicationWatcher,
        private val restApiServerBaseUrl: String,
        private val publishSubject: PublishSubject<Any> = PublishSubject.create(),
        private val okHttpClient: OkHttpClient = HttpClientFactory.makeOkHttpClient(false),

        var dbOpenHelper: DbOpenHelper = DbOpenHelper(context),
        var qiscusRestApi: QiscusRestApi = QiscusRestApiFactory.makeQiscusRestApi(restApiServerBaseUrl, okHttpClient),

        var accountLocal: AccountLocal = AccountLocalImpl(context),
        var accountRemote: AccountRemote = AccountRemoteImpl(accountLocal, qiscusRestApi),
        var accountRepository: AccountRepository = AccountDataRepository(accountLocal, accountRemote),

        var userLocal: UserLocal = UserLocalImpl(dbOpenHelper),
        var userRepository: UserRepository = UserDataRepository(userLocal),

        private val mimeTypeGuesser: MimeTypeGuesser = MimeTypeGuesserImpl(),
        private val fileUtil: FileUtil = FileUtil(context, mimeTypeGuesser),
        private val filePathGenerator: FilePathGenerator = fileUtil,
        private val fileManager: FileManager = fileUtil,
        var fileLocal: FileLocal = FileLocalImpl(dbOpenHelper),
        var fileRemote: FileRemote = FileRemoteImpl(accountLocal, restApiServerBaseUrl, okHttpClient,
                filePathGenerator),

        private val fileDataPusher: FileDataPusher = FileDataPusher(publishSubject),
        var filePublisher: FilePublisher= fileDataPusher,
        var fileSubscriber: FileSubscriber = fileDataPusher,
        var fileObserver: FileObserver = FileDataObserver(fileSubscriber),

        private val roomDataPusher: RoomDataPusher = RoomDataPusher(publishSubject, userLocal),
        var roomPublisher: RoomPublisher = roomDataPusher,
        var roomSubscriber: RoomSubscriber = roomDataPusher,

        var roomLocal: RoomLocal = RoomLocalImpl(dbOpenHelper, accountLocal, userLocal, roomPublisher),

        private val commentDataPusher: CommentDataPusher = CommentDataPusher(publishSubject),
        var commentPublisher: CommentPublisher = commentDataPusher,
        var commentSubscriber: CommentSubscriber = commentDataPusher,

        var commentLocal: CommentLocal = CommentLocalImpl(dbOpenHelper, accountLocal, roomLocal, userLocal, fileLocal, commentPublisher),

        var roomRemote: RoomRemote = RoomRemoteImpl(accountLocal, qiscusRestApi, roomLocal, commentLocal),
        var roomRepository: RoomRepository = RoomDataRepository(roomLocal, roomRemote),

        var commentRemote: CommentRemote = CommentRemoteImpl(accountLocal, qiscusRestApi),
        var postCommentHandler: PostCommentHandler = PostCommentHandlerImpl(commentLocal, commentRemote, fileRemote, filePublisher),
        var commentRepository: CommentRepository = CommentDataRepository(commentLocal, commentRemote,
                fileLocal, fileRemote, fileManager, filePublisher, postCommentHandler),

        var pubSubClient: QiscusPubSubClient = QiscusMqttClient(context, applicationWatcher = applicationWatcher,
                accountLocal = accountLocal, commentLocal = commentLocal, commentPayloadMapper = CommentPayloadMapper(),
                roomPublisher = roomPublisher, postCommentHandler = postCommentHandler, commentRemote = commentRemote),

        var roomObserver: RoomObserver = RoomDataObserver(pubSubClient, roomSubscriber),
        var commentObserver: CommentObserver = CommentDataObserver(pubSubClient, commentSubscriber)
)