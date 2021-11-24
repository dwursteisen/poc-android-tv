package your.game.systems

import com.github.dwursteisen.minigdx.Seconds
import com.github.dwursteisen.minigdx.ecs.components.SpriteComponent
import com.github.dwursteisen.minigdx.ecs.entities.Entity
import com.github.dwursteisen.minigdx.ecs.entities.position
import com.github.dwursteisen.minigdx.ecs.states.State
import com.github.dwursteisen.minigdx.ecs.systems.StateMachineSystem
import com.github.dwursteisen.minigdx.math.Vector3
import your.game.BadAnswerEvent
import your.game.GoodAnswerEvent
import your.game.Player
import your.game.StartMovingEvent
import your.game.StopMovingEvent
import your.game.systems.Constants.JUMP_HEIGHT
import your.game.systems.Constants.PLAYER_SPEED
import kotlin.math.cos
import kotlin.math.sin

class PlayerSystem : StateMachineSystem(Player::class) {

    inner class GoToPosition : Run() {

        override fun update(delta: Seconds, entity: Entity): State? {
            entity.position.addLocalTranslation(x = PLAYER_SPEED, delta = delta)
            if (entity.position.localTranslation.x in (-0.1f..0.1f)) {
                return Run()
            }
            return super.update(delta, entity)
        }

        override fun onExit(entity: Entity) {
            entity.position.setLocalTranslation(x = 0f)
            emit(StartMovingEvent())
        }
    }

    open inner class Run : State() {

        override fun onEnter(entity: Entity) {
            // Set animation to run
            entity.get(SpriteComponent::class).switchToAnimation("run")
        }

        override fun update(delta: Seconds, entity: Entity): State? {
            // Emit particles
            // if touch piece -> Wait
            return null
        }

        override fun configure(entity: Entity) {
            onEvent(StopMovingEvent::class) { _ ->
                Wait()
            }
        }
    }

    inner class Wait : State() {

        override fun onEnter(entity: Entity) {
            // Set animation to idle
            entity.get(SpriteComponent::class).switchToAnimation("idle")
        }

        override fun configure(entity: Entity) {
            onEvent(GoodAnswerEvent::class) { _ ->
                Jump()
            }
            onEvent(BadAnswerEvent::class) { _ ->
                Fail()
            }
        }
    }

    inner class Jump : State() {

        var t = 0f

        var y = 0f

        override fun onEnter(entity: Entity) {
            // Set animation to jump
            entity.get(SpriteComponent::class).switchToAnimation("jump_up")
            y = entity.position.localTranslation.y
        }

        override fun update(delta: Seconds, entity: Entity): State? {
            t += delta
            val currentY = entity.position.localTranslation.y
            // make the player jump
            entity.position.setLocalTranslation(y = y + sin(t * 6f) * JUMP_HEIGHT)
            // When going down, switch to the fall animation
            if(currentY > entity.position.localTranslation.y) {
                entity.get(SpriteComponent::class).switchToAnimation("jump_down")
            }

            return if(entity.position.localTranslation.y < y) {
                Run()
            } else {
                null
            }
        }

        override fun onExit(entity: Entity) {
            entity.position.setLocalTranslation(y = y)
            emit(StartMovingEvent())
        }
    }

    inner class Fail : State() {

        private var coolDown = 2f

        override fun onEnter(entity: Entity) {
            // make the dying animation
            entity.get(SpriteComponent::class).switchToAnimation("coin") // FIXME: rajouter une animation de fail
        }

        override fun update(delta: Seconds, entity: Entity): State? {
            coolDown -= delta
            // wait a bit before going back to run animation.
            return if(coolDown < 0f) {
                Run()
            } else {
                null
            }
        }
    }

    override fun initialState(entity: Entity): State {
        return GoToPosition()
    }
}
