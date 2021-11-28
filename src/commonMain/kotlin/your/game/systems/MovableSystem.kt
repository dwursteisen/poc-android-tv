package your.game.systems

import com.github.dwursteisen.minigdx.Seconds
import com.github.dwursteisen.minigdx.ecs.Engine
import com.github.dwursteisen.minigdx.ecs.entities.Entity
import com.github.dwursteisen.minigdx.ecs.entities.position
import com.github.dwursteisen.minigdx.ecs.states.State
import com.github.dwursteisen.minigdx.ecs.systems.StateMachineSystem
import your.game.Movable
import your.game.NextQuestionEvent
import your.game.StartMovingEvent
import your.game.StopMovingEvent

class MovableSystem : StateMachineSystem(Movable::class) {

    override fun onGameStarted(engine: Engine) {
        super.onGameStarted(engine)
        entities.forEach {
            it.position.setLocalTranslation(x = 10f)
        }
    }

    inner abstract class Move(targetX: Float = 0f) : State() {

        private val targetRange = ((targetX - 0.1f)..(targetX + 0.1f))

        override fun update(delta: Seconds, entity: Entity): State? {
            return if (entity.position.localTranslation.x in targetRange) {
                if (!nextMoveIsGoIn) {
                    Hold(goIn = false)
                } else {
                    GoIn()
                }
            } else {
                entity.position.addLocalTranslation(x = -Constants.PLAYER_SPEED, delta = delta)
                null
            }
        }

        abstract val nextMoveIsGoIn: Boolean
    }

    inner class GoIn : Move(0f) {

        override val nextMoveIsGoIn: Boolean = false

        override fun onEnter(entity: Entity) {
            entity.position.setLocalTranslation(x = 10f)
        }

        override fun onExit(entity: Entity) {
            entity.position.setLocalTranslation(x = 0f)
            emit(StopMovingEvent())
        }
    }

    inner class GoOut : Move(-10f) {

        override val nextMoveIsGoIn: Boolean = true

        override fun onExit(entity: Entity) {
            emit(NextQuestionEvent())
        }
    }

    inner class Hold(val goIn: Boolean = true) : State() {

        override fun configure(entity: Entity) {
            onEvent(StartMovingEvent::class) { _ ->
                if (goIn) {
                    GoIn()
                } else {
                    GoOut()
                }
            }
        }
    }

    override fun initialState(entity: Entity): State {
        return Hold()
    }
}
