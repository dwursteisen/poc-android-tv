package your.game

import com.github.dwursteisen.minigdx.GameContext
import com.github.dwursteisen.minigdx.Seconds
import com.github.dwursteisen.minigdx.ecs.Engine
import com.github.dwursteisen.minigdx.ecs.components.Color
import com.github.dwursteisen.minigdx.ecs.components.Component
import com.github.dwursteisen.minigdx.ecs.components.HorizontalAlignment
import com.github.dwursteisen.minigdx.ecs.components.StateMachineComponent
import com.github.dwursteisen.minigdx.ecs.components.TextComponent
import com.github.dwursteisen.minigdx.ecs.entities.EntityFactory
import com.github.dwursteisen.minigdx.ecs.events.Event
import com.github.dwursteisen.minigdx.ecs.systems.System
import com.github.dwursteisen.minigdx.file.Font
import com.github.dwursteisen.minigdx.file.get
import com.github.dwursteisen.minigdx.game.Game
import com.github.dwursteisen.minigdx.graph.GraphScene
import com.github.dwursteisen.minigdx.graph.Sprite
import kotlinx.serialization.ExperimentalSerializationApi
import your.game.systems.CoinSystem
import your.game.systems.CursorSystem
import your.game.systems.LabelSystem
import your.game.systems.MovableSystem
import your.game.systems.PlayerSystem
import your.game.systems.QuestionSelectorSystem

class Question(
    val label: String,
    val firstLabel: String,
    val secondLabel: String,
    val firstLabelAnswer: Boolean
)

enum class LabelType {
    QUESTION,
    ANSWER1,
    ANSWER2
}

class Player : StateMachineComponent()
class Label(val type: LabelType) : StateMachineComponent()
class Cursor : StateMachineComponent()
class Coin(var t: Seconds = 0f) : Component

// Move automatically
class Movable : StateMachineComponent()

class NextQuestionEvent: Event
class ShowQuestionEvent(val question: Question) : Event
class SelectAnswerEvent(val firstAnswer: Boolean) : Event

class GoodAnswerEvent : Event
class BadAnswerEvent : Event

class StopMovingEvent : Event
class StartMovingEvent : Event

class TouchCoinEvent : Event

@OptIn(ExperimentalStdlibApi::class)
class MyGame(override val gameContext: GameContext) : Game {

    private val scene by gameContext.fileHandler.get<GraphScene>("level.protobuf")
    private val sprite by gameContext.fileHandler.get<Sprite>("2d-platformer-spr.protobuf")

    private val font by gameContext.fileHandler.get<Font>("font")

    val questions = listOf(
        Question(
            label = "... voiture",
            secondLabel = "un",
            firstLabel = "une",
            firstLabelAnswer = true
        ),

        Question(
            label = "3 x 4 =",
            firstLabel = "12",
            secondLabel = "14",
            firstLabelAnswer = true
        )
    )

    override val clearColor: Color = Color(1f, 0f, 0f)

    @OptIn(ExperimentalSerializationApi::class)
    override fun createEntities(entityFactory: EntityFactory) {
        // Create all entities needed at startup
        // The scene is the node graph that can be updated in Blender

        entityFactory.exploreChildren = false

        scene.traverse { node, parent ->
            if (node.name.startsWith("Plane")) {
                val entity = entityFactory.createFromNode(node)
                entity.add(Movable())
            } else if (node.name.startsWith("label")) {
                val entity = entityFactory.createText("ashtashtasht", font, node)
                val type = if (node.name.contains("001")) {
                    LabelType.ANSWER1
                } else {
                    LabelType.ANSWER2
                }
                entity.add(Label(type = type))
                entity.attachTo(parent)
            } else if (node.name.startsWith("question")) {
                val entity = entityFactory.createText("thisisaverylongtext", font, node)
                entity.add(Label(type = LabelType.QUESTION))
                entity.get(TextComponent::class).horizontalAlign = HorizontalAlignment.Center
                entity.attachTo(parent)
                entity
            } else if (node.name.startsWith("player")) {
                val entity = entityFactory.createSprite(sprite, node.combinedTransformation)
                entity.add(Player())
            } else if (node.name.startsWith("coin")) {
                val entity = entityFactory.createSprite(sprite, node.combinedTransformation)
                entity.add(Coin())
                entity.attachTo(parent)
            } else if (node.name.startsWith("cursor")) {
                val entity = entityFactory.createFromNode(node)
                entity.add(Cursor())
            } else {
                entityFactory.createFromNode(node).attachTo(parent)
            }
        }
    }

    override fun createSystems(engine: Engine): List<System> {
        // Create all systems used by the game
        return listOf(
            CursorSystem(),
            LabelSystem(),
            QuestionSelectorSystem(questions),
            PlayerSystem(),
            CoinSystem(),
            MovableSystem()
        )
    }
}
