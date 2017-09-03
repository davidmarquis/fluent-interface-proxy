package com.fluentinterface;

import com.fluentinterface.builder.Builder;
import com.fluentinterface.proxy.BuilderState;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CustomInstantiatorTest {

    @Test
    public void canUseCustomInstantiatorToCreateObjectInstance() {
        assertThat(anAnimal().withName("Max").build(), allOf(
                hasProperty("name", is("Max")),
                hasProperty("species", is("NONE"))
        ));

        assertThat(anAnimal().withName("Max").withSpecies("Dog").build(), allOf(
                hasProperty("name", is("Max")),
                hasProperty("species", is("Dog")),
                hasProperty("legs", is(0))
        ));

        assertThat(anAnimal().withName("Max").withSpecies("Dog").withLegs(4).build(), allOf(
                hasProperty("name", is("Max")),
                hasProperty("species", is("Dog")),
                hasProperty("legs", is(4))
        ));
    }

    private AnimalBuilder anAnimal() {
        return ReflectionBuilder.implementationFor(AnimalBuilder.class)
                    .usingInstantiator((BuilderState state) -> {
                        if (state.hasValueFor("legs")) {
                            return new Animal(
                                    state.consume("name", String.class).orElse(null),
                                    state.consume("species", String.class).orElse("NONE"),
                                    state.consume("legs", int.class).orElse(2)
                            );
                        } else {
                            return new Animal(
                                    state.consume("name", String.class).orElse(null),
                                    state.consume("species", String.class).orElse("NONE")
                            );
                        }
                    })
                    .create();
    }

    public interface AnimalBuilder extends Builder<Animal> {
        AnimalBuilder withName(String name);
        AnimalBuilder withSpecies(String species);
        AnimalBuilder withLegs(int legs);
    }

    public static class Animal {
        String name;
        String species;
        int legs;

        public Animal(String name, String species) {
            this.name = name;
            this.species = species;
        }

        public Animal(String name, String species, int legs) {
            this(name, species);
            this.legs = legs;
        }

        public String getName() {
            return name;
        }

        public String getSpecies() {
            return species;
        }

        public int getLegs() {
            return legs;
        }

        @Override
        public String toString() {
            return "Animal{" +
                    "name='" + name + '\'' +
                    ", species='" + species + '\'' +
                    ", legs=" + legs +
                    '}';
        }
    }
}
