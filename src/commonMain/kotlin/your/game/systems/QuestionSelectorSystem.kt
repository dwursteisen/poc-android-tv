package your.game.systems

import com.github.dwursteisen.minigdx.Seconds
import com.github.dwursteisen.minigdx.ecs.Engine
import com.github.dwursteisen.minigdx.ecs.components.StateMachineComponent
import com.github.dwursteisen.minigdx.ecs.entities.Entity
import com.github.dwursteisen.minigdx.ecs.events.Event
import com.github.dwursteisen.minigdx.ecs.systems.EntityQuery
import com.github.dwursteisen.minigdx.ecs.systems.System
import com.github.dwursteisen.minigdx.input.Key
import your.game.BadAnswerEvent
import your.game.Cursor
import your.game.GoodAnswerEvent
import your.game.NextQuestionEvent
import your.game.Question
import your.game.SelectAnswerEvent
import your.game.ShowQuestionEvent

class QuestionSelectorSystem(var questions: List<Question>) : System(EntityQuery.none()) {

    var currentQuestion: Question? = null

    var firstAnswer: Boolean = false

    val cursor by interested(EntityQuery.of(Cursor::class))

    override fun onGameStarted(engine: Engine) {
        currentQuestion = questions.firstOrNull()
        questions = questions.drop(1)
        currentQuestion?.run { emit(ShowQuestionEvent(this)) }
    }

    override fun update(delta: Seconds) {
        if (input.isKeyJustPressed(Key.SPACE) || input.isKeyJustPressed(Key.DPAD_CENTER)) {
            if (!cursor.first().get(Cursor::class).hasCurrentState(CursorSystem.Select::class)) {
                return
            }

            if (currentQuestion?.firstLabelAnswer == true && firstAnswer) {
                emit(GoodAnswerEvent())
            } else {
                emit(BadAnswerEvent())
            }
        }
    }

    override fun onEvent(event: Event, entityQuery: EntityQuery?) {
        if(event is SelectAnswerEvent) {
            firstAnswer = event.firstAnswer
        } else if(event is NextQuestionEvent) {
            currentQuestion = questions.firstOrNull()
            if(currentQuestion == null) {
                TODO("End of the game!")
            } else {
       //         questions = questions.drop(1)
                currentQuestion?.run { emit(ShowQuestionEvent(this)) }
            }
        }
    }

    override fun update(delta: Seconds, entity: Entity) = Unit
}
