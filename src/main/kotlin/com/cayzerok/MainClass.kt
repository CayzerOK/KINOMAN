package com.cayzerok

import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.requests.restaction.pagination.ReactionPaginationAction
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.util.*
import javax.security.auth.login.LoginException

val random = Random()
val emote = System.getenv("EMOTE")
val emotename = System.getenv("EMOTE_NAME")
var movielist = mutableListOf<String>()

fun addToList(movie:String) {
    if (!movielist.contains(movie)) movielist.add(movie)
}

@SpringBootApplication
class MainListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent?) {
        val author = event!!.author
        val channel = event.channel
        val message = event.message
        val msg = message.contentDisplay
        val bot = author.isBot
        if (event.isFromType(ChannelType.TEXT) && !bot)
        when {
            msg.endsWith(":$emotename:") -> {
                addToList(msg.dropLast(emotename.length + 2))
                movielist.forEach { println(it) }
                println("${author.name}, фильм добавлен:${msg.dropLast(emotename.length+2)}")
                message.addReaction(emote).queue()
            }
            msg.startsWith("@КИНМАН") -> {
                val movie = movielist[random.nextInt(movielist.lastIndex)]
                println(movie)
                channel.sendMessage(movie)
            }
        }
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent?) {
        val ID = event!!.reaction.messageId
        val channel = event.channel
        val message = channel.getMessageById(ID).complete()
        val author = message.author
        if (event.isFromType(ChannelType.TEXT))
            when {
                event.reactionEmote.name == emotename && checkUsers(event.reaction.users) -> {
                    addToList(message.contentDisplay)
                    println("${author.name}, фильм добавлен: ${message.contentDisplay}")
                    message.addReaction(event.reactionEmote.emote).queue()
                    println(event.reactionEmote.emote)
                }
            }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(MainListener::class.java, *args)
            println("Token is "+System.getenv("TOKEN"))
            try {
                val jda =
                        JDABuilder(System.getenv("TOKEN"))
                                .addEventListener(MainListener())
                                .build()
                println("Token accepted.")
                jda.awaitReady()

                println("Finished Building JDA!")
            } catch (e: LoginException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}

fun checkUsers(users:ReactionPaginationAction) : Boolean {
    users.forEach {
        if (it.isBot) return false
    }
    return true
}