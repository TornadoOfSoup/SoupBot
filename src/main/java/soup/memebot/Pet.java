package soup.memebot;

import com.google.gson.annotations.SerializedName;

/**
 * Created by RPGenius on 8/9/2017.
 */
public class Pet {
    Species species;
    Enum breed;
    String name;
    int strength, vitality, charisma, agility, intelligence;
    int exp, level;

    public Pet(Species petSpecies) {
        species = petSpecies;
        name = Utils.toProperCase(species.name().replace('_', ' '));
        breed = Breed.getBreed(species);
        level = 1;
        exp = 0;
        System.out.println("Breed for new " + species.name() + " has been set to " + breed.name());
        calculateBaseStats();
    }

    public Pet(Species petSpecies, String petName) {
        species = petSpecies;
        name = petName;
        breed = Breed.getBreed(species);
        level = 1;
        exp = 0;
        System.out.println("Breed for new " + species.name() + " has been set to " + breed.name());
        calculateBaseStats();
    }

    public Pet() {

    }

    private void calculateBaseStats() {
        if (species.equals(Species.DOG)) {
            if (breed.equals(Breed.DogBreeds.GOLDEN)) {
                setStats(75, 65, 75, 70, 70);
            } else if (breed.equals(Breed.DogBreeds.PUG)) {
                setStats(45, 80, 90, 45, 85);
            } else if (breed.equals(Breed.DogBreeds.SHEPHERD)) {
                setStats(80, 70, 70, 70, 90);
            } else if (breed.equals(Breed.DogBreeds.SHIBA)) {
                setStats(65, 80, 90, 65, 65);
            } else if (breed.equals(Breed.DogBreeds.CHIHUAHUA)) {
                setStats(60, 90, 80, 60, 60);
            }
        } else if (species.equals(Species.CAT)) {
            if (breed.equals(Breed.CatBreeds.SIAMESE)) {
                setStats(65, 70, 80, 75, 85);
            } else if (breed.equals(Breed.CatBreeds.PERSIAN)) {
                setStats(55, 65, 80, 60, 55);
            } else if (breed.equals(Breed.CatBreeds.SPHYNX)) {
                setStats(65, 60, 90, 70, 80);
            } else if (breed.equals(Breed.CatBreeds.SHORTHAIR)) {
                setStats(65, 85, 75, 70, 80);
            } else if (breed.equals(Breed.CatBreeds.NYAN)) {
                setStats(90, 30, 100, 100, 30);
            }
        } else {
                setStats(50, 50, 50, 50, 50);
        }
    }

    public void setStats(int strength, int vitality, int charisma, int agility, int intelligence) {
        this.strength = strength + Utils.plusMinus(3);
        this.vitality = vitality + Utils.plusMinus(3);
        this.charisma = charisma + Utils.plusMinus(3);
        this.agility = agility + Utils.plusMinus(3);
        this.intelligence = intelligence + Utils.plusMinus(3);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Species getSpecies() {
        return this.species;
    }

    public Enum getBreed() {
        return this.breed;
    }

    public int getStrength() {
        return this.strength;
    }

    public int getVitality() {
        return this.vitality;
    }

    public int getCharisma() {
        return this.charisma;
    }

    public int getAgility() {
        return this.agility;
    }

    public int getIntelligence() {
        return this.intelligence;
    }

    public int getExp() {
        return this.exp;
    }

    public int getLevel() {
        return this.level;
    }
}
