package your.game.systems

import com.github.dwursteisen.minigdx.Seconds
import com.github.dwursteisen.minigdx.ecs.components.ModelComponent
import com.github.dwursteisen.minigdx.ecs.components.SpriteComponent
import com.github.dwursteisen.minigdx.ecs.entities.Entity
import com.github.dwursteisen.minigdx.ecs.entities.position
import com.github.dwursteisen.minigdx.ecs.events.Event
import com.github.dwursteisen.minigdx.ecs.systems.EntityQuery
import com.github.dwursteisen.minigdx.ecs.systems.System
import com.github.dwursteisen.minigdx.math.Interpolations
import your.game.Coin
import your.game.CoinRotation
import your.game.NextQuestionEvent
import your.game.TouchCoinEvent

class CoinRotationSystem : System(EntityQuery.Companion.of(CoinRotation::class)) {

    override fun onEntityAdded(entity: Entity) {
        entity.get(SpriteComponent::class).switchToAnimation("coin")
    }

    private val duration = 2f

    override fun update(delta: Seconds, entity: Entity) {
        val coin = entity.get(CoinRotation::class)
        coin.t += delta

        if (coin.t > duration) {
            coin.t -= duration
        }

        entity.position.setLocalRotation(
            x = 0f,
            y = Interpolations.linear.interpolate(0f, 360f, coin.t / duration),
            z = 0f

        )
    }
}

class CoinSystem : System(EntityQuery.of(Coin::class)) {

    override fun update(delta: Seconds, entity: Entity) = Unit

    override fun onEvent(event: Event, entityQuery: EntityQuery?) {
        if (event is TouchCoinEvent) {
            entities.forEach {
                it.get(ModelComponent::class).hidden = true
            }
        } else if (event is NextQuestionEvent) {
            entities.forEach {
                it.get(ModelComponent::class).hidden = false
            }
        }
    }
}
