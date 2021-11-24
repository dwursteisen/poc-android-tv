package your.game.systems

import com.github.dwursteisen.minigdx.ecs.components.ModelComponent
import com.github.dwursteisen.minigdx.ecs.components.TextComponent
import com.github.dwursteisen.minigdx.ecs.entities.Entity
import com.github.dwursteisen.minigdx.ecs.states.State
import com.github.dwursteisen.minigdx.ecs.systems.StateMachineSystem
import your.game.Label
import your.game.LabelType
import your.game.ShowQuestionEvent

class LabelSystem : StateMachineSystem(Label::class) {

    inner class Hide : State() {

        override fun onEnter(entity: Entity) {
            entity.get(ModelComponent::class).model.displayble = false
        }

        override fun onExit(entity: Entity) {
            entity.get(ModelComponent::class).model.displayble = true
        }

        override fun configure(entity: Entity) {
            onEvent(ShowQuestionEvent::class) { event ->
                val label = when (entity.get(Label::class).type) {
                    LabelType.QUESTION -> event.question.label
                    LabelType.ANSWER1 -> event.question.firstLabel
                    LabelType.ANSWER2 -> event.question.secondLabel
                }

                Ask(label)
            }
        }
    }

    inner class Ask(val label: String) : State() {

        override fun onEnter(entity: Entity) {
            entity.get(TextComponent::class).text.content = label
            entity.get(ModelComponent::class).model.displayble = true
        }

        override fun onExit(entity: Entity) {
            entity.get(ModelComponent::class).model.displayble = false
        }

        override fun configure(entity: Entity) {
            onEvent(ShowQuestionEvent::class) { event ->
                val label = when (entity.get(Label::class).type) {
                    LabelType.QUESTION -> event.question.label
                    LabelType.ANSWER1 -> event.question.firstLabel
                    LabelType.ANSWER2 -> event.question.secondLabel
                }

                Ask(label)
            }
        }
    }

    override fun initialState(entity: Entity): State {
        return Hide()
    }
}
