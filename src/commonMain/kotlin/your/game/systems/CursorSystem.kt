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
import your.game.StopMovingEvent

class CursorSystem : StateMachineSystem(Cursor::class) {

    inner class Hide : State() {

        override fun onEnter(entity: Entity) {
            entity.get(ModelComponent::class).model.displayble = false
        }

        override fun update(delta: Seconds, entity: Entity): State? {
            if (input.isKeyJustPressed(Key.SPACE)) {
                return Select()
            }
            return null
        }

        override fun onExit(entity: Entity) {
            entity.get(ModelComponent::class).model.displayble = true
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

        override fun onEnter(entity: Entity) {
            entity.attachTo(movables.firstOrNull())
            emit(SelectAnswerEvent(position == FIRST_POSITION))
        }

        override fun update(delta: Seconds, entity: Entity): State? {
            entity.position.setLocalTranslation(x = Interpolations.lerp(position, entity.position.localTranslation.x))

            if (input.isKeyJustPressed(Key.ARROW_LEFT)) {
                return Select(FIRST_POSITION)
            } else if (input.isKeyJustPressed(Key.ARROW_RIGHT)) {
                return Select(SECOND_POSITION)
            }
            return null
        }
    }

    override fun initialState(entity: Entity): State {
        return Hide()
    }

    companion object {

        private const val FIRST_POSITION = -1.8f
        private const val SECOND_POSITION = 1.8f
    }
}
