package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.absent

import ianmorgan.graphstore.graphql.ArgsWalker
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object ArgsWalkerSpec : Spek({

    val example = mapOf(
        "name" to emptyMap<String, Any>(),
        "friends" to mapOf("length" to 10),
        "friends/name" to emptyMap(),
        "friends/friends" to emptyMap(),
        "friends/friends/appearsIn" to emptyMap(),
        "starships" to emptyMap(),
        "starships/model" to emptyMap()
    )

    describe("walking the query args") {

        it("should walk simple args") {

            val root = ArgsWalker( example)

            // friends
            val friends = root.walkPath("friends")
            val fiendsArgs = mapOf("/" to  mapOf("length" to 10),
                "name" to emptyMap(),
                "friends" to emptyMap(),
                "friends/appearsIn" to emptyMap()) as Map<String,Map<String,Any>>

            assert.that(friends.args(), equalTo(fiendsArgs))
            assert.that(friends.node(), equalTo("friends"))
            assert.that(friends.fullPath(), equalTo("friends"))
            assert.that(friends.isRoot(), equalTo(false))
            assert.that(friends.parent(), equalTo(root))

            // friends of friends
            val friendsOfFriends = friends.walkPath("friends")
            val fiendsOfFriendsArgs = mapOf("appearsIn" to emptyMap<String,Any>())

            assert.that(friendsOfFriends.args(), equalTo(fiendsOfFriendsArgs))
            assert.that(friendsOfFriends.node(), equalTo("friends"))
            assert.that(friendsOfFriends.fullPath(), equalTo("friends/friends"))
            assert.that(friendsOfFriends.isRoot(), equalTo(false))
            assert.that(friendsOfFriends.parent(), equalTo(friends))
        }

        it("should build root node") {
            val root = ArgsWalker(example)

            assert.that(root.args(), equalTo(example))
            assert.that(root.node(), equalTo("root"))
            assert.that(root.isRoot(), equalTo(true))
            assert.that(root.parent(), absent())
        }

        it("should list children") {
            val root = ArgsWalker(example)
            val children = root.children()

            assert.that(children.size, equalTo(2))
            assert.that(children[0].node(), equalTo("friends"))
            assert.that(children[1].node(), equalTo("starships"))
        }

        it("should replace node args") {
            val root = ArgsWalker(example)
            val friends = root.walkPath("friends")

            val aFriend = friends.replaceNodeArgs(mapOf<String,Any>("length" to 10))
            assert.that(aFriend.hasNodeArgs(),equalTo(true))
            assert.that(aFriend.args()["/"], equalTo(mapOf<String,Any>("length" to 10)))

            // these should stay unaltered
            assert.that(aFriend.fullPath(), equalTo(friends.fullPath()))
            assert.that(aFriend.parent(), equalTo(friends.parent()))
            assert.that(aFriend.node(), equalTo(friends.node()))

        }

    }

})
