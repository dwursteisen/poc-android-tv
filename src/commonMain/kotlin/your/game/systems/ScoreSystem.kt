package your.game.systems

import com.github.dwursteisen.minigdx.Seconds
import com.github.dwursteisen.minigdx.ecs.components.TextComponent
import com.github.dwursteisen.minigdx.ecs.entities.Entity
import com.github.dwursteisen.minigdx.ecs.events.Event
import com.github.dwursteisen.minigdx.ecs.systems.EntityQuery
import com.github.dwursteisen.minigdx.ecs.systems.System
import your.game.Score
import your.game.TouchCoinEvent

class ScoreSystem : System(EntityQuery.of(Score::class, TextComponent::class)) {

    override fun update(delta: Seconds, entity: Entity) = Unit

    override fun onEvent(event: Event, entityQuery: EntityQuery?) {
        if (event is TouchCoinEvent) {
            entities.forEach {
                it.get(Score::class).score += 1
                val score = it.get(Score::class).score
                it.get(TextComponent::class).text.content = "x $score"
            }
        }
    }
}
