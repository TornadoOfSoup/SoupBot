package soup.memebot;

import java.util.Random;

/**
 * Created by RPGenius on 8/10/2017.
 */
public class Breed {
    public Breed() {

    }

    public static Enum getBreed(Species species) {
        Random rand = new Random();
        switch (species) {
            case DOG:
                switch (rand.nextInt(5)) {
                    case 0:
                        return DogBreeds.GOLDEN;
                    case 1:
                        return DogBreeds.PUG;
                    case 2:
                        return DogBreeds.SHEPHERD;
                    case 3:
                        return DogBreeds.SHIBA;
                    case 4:
                        return DogBreeds.CHIHUAHUA;
                }
            case CAT:
                switch (rand.nextInt(5)) {
                    case 0:
                        return CatBreeds.SIAMESE;
                    case 1:
                        return CatBreeds.PERSIAN;
                    case 2:
                        return CatBreeds.SPHYNX;
                    case 3:
                        return CatBreeds.SHORTHAIR;
                    case 4:
                        return CatBreeds.NYAN;
                }
            case SNAKE:
                switch (rand.nextInt(5)) {
                    case 0:
                        return SnakeBreeds.RATTLE;
                    case 1:
                        return SnakeBreeds.COBRA;
                    case 2:
                        return SnakeBreeds.GARTER;
                    case 3:
                        return SnakeBreeds.PYTHON;
                    case 4:
                        return SnakeBreeds.DANGER;
                }
            case MOUSE:
                switch (rand.nextInt(5)) {
                    case 0:
                        return MouseBreeds.HOUSE;
                    case 1:
                        return MouseBreeds.WOOD;
                    case 2:
                        return MouseBreeds.DEER;
                    case 3:
                        return MouseBreeds.WHITE;
                    case 4:
                        return MouseBreeds.MICKEY;
                }
            case DRAGON:
                switch (rand.nextInt(5)) {
                    case 0:
                        return DragonBreeds.RED_WYVERN;
                    case 1:
                        return DragonBreeds.BLACK_WYVERN;
                    case 2:
                        return DragonBreeds.SILVER_WYVERN;
                    case 3:
                        return DragonBreeds.GOLD_WYVERN;
                    case 4:
                        return DragonBreeds.WORLD_EATER;
                }
            case WHALE:
                switch (rand.nextInt(5)) {
                    case 0:
                        return WhaleBreeds.BELUGA;
                    case 1:
                        return WhaleBreeds.ORCA;
                    case 2:
                        return WhaleBreeds.SPERM;
                    case 3:
                        return WhaleBreeds.BLUE;
                    case 4:
                        return WhaleBreeds.BUTTERWHALE;
                }
            case TURTLE:
                switch (rand.nextInt(5)) {
                    case 0:
                        return TurtleBreeds.BOX;
                    case 1:
                        return TurtleBreeds.SNAPPING;
                    case 2:
                        return TurtleBreeds.SPOTTED;
                    case 3:
                        return TurtleBreeds.NINJA;
                    case 4:
                        return TurtleBreeds.KOOPA;
                }
            case FISH:
                switch (rand.nextInt(5)) {
                    case 0:
                        return FishBreeds.GOLDFISH;
                    case 1:
                        return FishBreeds.CLOWN;
                    case 2:
                        return FishBreeds.SALMON;
                    case 3:
                        return FishBreeds.TUNA;
                    case 4:
                        return FishBreeds.KOI;
                }
            default:
                return Unknown.UNKNOWN;
        }
    }



    enum DogBreeds {
        GOLDEN,
        PUG,
        SHEPHERD,
        SHIBA,
        CHIHUAHUA;
    }

    enum CatBreeds {
        SIAMESE,
        PERSIAN,
        SPHYNX,
        SHORTHAIR,
        NYAN;
    }

    enum SnakeBreeds {
        RATTLE,
        COBRA,
        GARTER,
        PYTHON,
        DANGER;
    }

    enum MouseBreeds {
        HOUSE,
        WOOD,
        DEER,
        WHITE,
        MICKEY;
    }

    enum DragonBreeds {
        RED_WYVERN,
        BLACK_WYVERN,
        SILVER_WYVERN,
        GOLD_WYVERN,
        WORLD_EATER;
    }

    enum WhaleBreeds {
        BELUGA,
        ORCA,
        SPERM,
        BLUE,
        BUTTERWHALE;
    }

    enum TurtleBreeds {
        BOX,
        SNAPPING,
        SPOTTED,
        NINJA,
        KOOPA;
    }

    enum FishBreeds {
        GOLDFISH,
        CLOWN,
        SALMON,
        TUNA,
        KOI;
    }

    enum Unknown {
        UNKNOWN;
    }
}


