package com.leochat.core.errors

/** Minimal Either used by repositories: `Either<AppFailure, T>`. */
sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    val isRight: Boolean get() = this is Right
    val isLeft: Boolean get() = this is Left

    fun getOrNull(): R? = (this as? Right)?.value
    fun leftOrNull(): L? = (this as? Left)?.value

    inline fun <T> fold(ifLeft: (L) -> T, ifRight: (R) -> T): T = when (this) {
        is Left -> ifLeft(value)
        is Right -> ifRight(value)
    }

    inline fun <T> map(transform: (R) -> T): Either<L, T> = when (this) {
        is Left -> this
        is Right -> Right(transform(value))
    }

    inline fun onRight(action: (R) -> Unit): Either<L, R> = also { if (it is Right) action(it.value) }
    inline fun onLeft(action: (L) -> Unit): Either<L, R> = also { if (it is Left) action(it.value) }
}

typealias AppResult<T> = Either<AppFailure, T>

fun <T> success(value: T): AppResult<T> = Either.Right(value)
fun failure(value: AppFailure): AppResult<Nothing> = Either.Left(value)

inline fun <L, R, T> Either<L, R>.flatMap(transform: (R) -> Either<L, T>): Either<L, T> = when (this) {
    is Either.Left -> this
    is Either.Right -> transform(value)
}
