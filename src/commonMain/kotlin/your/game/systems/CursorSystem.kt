package your.game.systems

import com.github.dwursteisen.minigdx.Seconds
import com.github.dwursteisen.minigdx.ecs.components.ModelComponent
import com.github.dwursteisen.minigdx.ecs.entities.Entity
import com.github.dwursteisen.minigdx.ecs.entities.position
import com.github.dwursteisen.minigdx.ecs.states.State
import com.github.dwursteisen.minigdx.ecs.systems.EntityQuery
import com.github.dwursteisen.minigdx.ecs.systems.StateMachineSystem
import com.github.dwursteisen.minigdx.input.Key
import com.github.dwursteisen.minigdx.math.Interpolations
import your.game.Cursor
import your.game.Movable
import your.game.SelectAnswerEvent
import your.game.StartMovingEvent
import your.game.StopMovingEvent
import kotlin.math.abs
import kotlin.math.sin

class CursorSystem : StateMachineSystem(Cursor::class) {

    inner class Hide : State() {

        override fun onEnter(entity: Entity) {
            entity.get(ModelComponent::class).hidden = true
        }

        override fun update(delta: Seconds, entity: Entity): State? {
            if (input.isKeyJustPressed(Key.SPACE)) {
                return Select()
            }
            return null
        }

        override fun onExit(entity: Entity) {
            entity.get(ModelComponent::class).hidden = false
        }

        override fun configure(entity: Entity) {
            onEvent(StopMovingEvent::class) {
                Select(FIRST_POSITION)
            }
        }

        // on event stop moving : show cursor
    }

    inner class Select(val position: Float = FIRST_POSITION) : State() {

        val movables by interested(EntityQuery.Companion.of(Movable::class))

        var y = 0f

        val cooldown: Seconds = 3f
        val bump: Seconds = 1f
        var t: Seconds = bump + cooldown

        var ttt = 0f

        override fun onEnter(entity: Entity) {
            entity.attachTo(movables.firstOrNull())
            emit(SelectAnswerEvent(position == FIRST_POSITION))
            y = entity.position.localTranslation.y
        }

        override fun update(delta: Seconds, entity: Entity): State? {
            entity.position.setLocalTranslation(x = Interpolations.lerp(position, entity.position.localTranslation.x))

            t -= delta
            if (t <= 0f) {
                t += cooldown + bump
                ttt = 0f
            } else if (t > bump) { // cooldown
                entity.position.setLocalTranslation(y = y)
            } else {
                ttt += delta
                entity.position.setLocalTranslation(y = y + abs(sin(ttt * 12)) * 0.2f)
            }

            if (input.isKeyJustPressed(Key.ARROW_LEFT)) {
                return Select(FIRST_POSITION)
            } else if (input.isKeyJustPressed(Key.ARROW_RIGHT)) {
                return Select(SECOND_POSITION)
            }
            return null
        }

        override fun configure(entity: Entity) {
            onEvent(StartMovingEvent::class) {
                Hide()
            }
        }

        override fun onExit(entity: Entity) {
            entity.position.setLocalTranslation(y = y)
        }
    }

    override fun initialState(entity: Entity): State {
        return Hide()
    }

    companion object {

        private const val FIRST_POSITION = -4.8f
        private const val SECOND_POSITION = 0.8f
    }
}
