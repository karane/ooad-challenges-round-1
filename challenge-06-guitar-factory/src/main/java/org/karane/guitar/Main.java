package org.karane.guitar;

import org.karane.guitar.domain.*;
import org.karane.guitar.factory.GuitarFactory;
import org.karane.guitar.inventory.GuitarInventory;
import org.karane.guitar.inventory.SearchCriteria;

public class Main {

    public static void main(String[] args) {

        var electricFactory = new GuitarFactory(GuitarType.ELECTRIC);
        var premiumElectric = new GuitarFactory(GuitarType.ELECTRIC, 40);
        var acousticFactory = new GuitarFactory(GuitarType.ACOUSTIC);
        var bassFactory     = new GuitarFactory(GuitarType.BASS);

        var inventory = new GuitarInventory();

        var strat = electricFactory.create(
                GuitarSpec.builder(GuitarType.ELECTRIC)
                        .bodyStyle(BodyStyle.STRATOCASTER)
                        .bodyWood(WoodType.ALDER)
                        .neckWood(WoodType.MAPLE)
                        .fretboardWood(WoodType.MAPLE)
                        .pickups(GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.SINGLE_COIL)
                        .finish(new Finish("Sunburst", Finish.Type.GLOSS))
                        .build());

        var lesPaul = premiumElectric.create(
                GuitarSpec.builder(GuitarType.ELECTRIC)
                        .bodyStyle(BodyStyle.LES_PAUL)
                        .bodyWood(WoodType.MAHOGANY)
                        .neckWood(WoodType.MAHOGANY)
                        .fretboardWood(WoodType.EBONY)
                        .pickups(GuitarSpec.PickupType.HUMBUCKER, GuitarSpec.PickupType.HUMBUCKER)
                        .finish(new Finish("Cherry", Finish.Type.GLOSS))
                        .build());

        var sg = electricFactory.create(
                GuitarSpec.builder(GuitarType.ELECTRIC)
                        .bodyStyle(BodyStyle.SG)
                        .bodyWood(WoodType.MAHOGANY)
                        .neckWood(WoodType.MAHOGANY)
                        .fretboardWood(WoodType.ROSEWOOD)
                        .pickups(GuitarSpec.PickupType.P90, GuitarSpec.PickupType.P90)
                        .finish(new Finish("Vintage White", Finish.Type.SATIN))
                        .build());

        var acoustic = acousticFactory.create(
                GuitarSpec.builder(GuitarType.ACOUSTIC)
                        .bodyStyle(BodyStyle.DREADNOUGHT)
                        .bodyWood(WoodType.SPRUCE)
                        .neckWood(WoodType.MAHOGANY)
                        .fretboardWood(WoodType.ROSEWOOD)
                        .finish(new Finish("Natural", Finish.Type.SATIN))
                        .build());

        var bass = bassFactory.create(
                GuitarSpec.builder(GuitarType.BASS)
                        .bodyStyle(BodyStyle.JAZZ_BASS)
                        .bodyWood(WoodType.ALDER)
                        .neckWood(WoodType.MAPLE)
                        .fretboardWood(WoodType.ROSEWOOD)
                        .pickups(GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.SINGLE_COIL)
                        .stringCount(4)
                        .finish(new Finish("Olympic White", Finish.Type.GLOSS))
                        .build());

        var flyingV7 = electricFactory.create(
                GuitarSpec.builder(GuitarType.ELECTRIC)
                        .bodyStyle(BodyStyle.FLYING_V)
                        .bodyWood(WoodType.BASSWOOD)
                        .neckWood(WoodType.MAPLE)
                        .fretboardWood(WoodType.EBONY)
                        .pickups(GuitarSpec.PickupType.HUMBUCKER, GuitarSpec.PickupType.HUMBUCKER)
                        .stringCount(7)
                        .finish(new Finish("Matte Black", Finish.Type.MATTE))
                        .build());

        for (var g : new Guitar[]{strat, lesPaul, sg, acoustic, bass, flyingV7}) {
            inventory.add(g);
        }

        printSection("All guitars in inventory");
        inventory.all().forEach(g -> System.out.println("  " + g));

        printSection("Search: available guitars with mahogany body");
        inventory.search(SearchCriteria.builder().bodyWood(WoodType.MAHOGANY).build())
                 .forEach(g -> System.out.println("  " + g));

        printSection("Search: available 7-string electrics");
        inventory.search(SearchCriteria.builder()
                        .type(GuitarType.ELECTRIC).stringCount(7).build())
                 .forEach(g -> System.out.println("  " + g));

        printSection("Reserve and sell the Les Paul (" + lesPaul.serialNumber() + ")");
        inventory.reserve(lesPaul);
        System.out.println("  After reserve: " + lesPaul);
        inventory.sell(lesPaul);
        System.out.println("  After sell:    " + lesPaul);

        printSection("Inventory stats");
        System.out.printf("  Available : %d%n", inventory.countByStatus(Guitar.Status.AVAILABLE));
        System.out.printf("  Reserved  : %d%n", inventory.countByStatus(Guitar.Status.RESERVED));
        System.out.printf("  Sold      : %d%n", inventory.countByStatus(Guitar.Status.SOLD));

        printSection("Validation examples");
        tryCreate("Acoustic with pickup (should fail)", () ->
                acousticFactory.create(
                        GuitarSpec.builder(GuitarType.ACOUSTIC)
                                .bodyStyle(BodyStyle.DREADNOUGHT)
                                .bodyWood(WoodType.SPRUCE)
                                .neckWood(WoodType.MAHOGANY)
                                .fretboardWood(WoodType.ROSEWOOD)
                                .pickups(GuitarSpec.PickupType.SINGLE_COIL)
                                .finish(new Finish("Natural", Finish.Type.SATIN))
                                .build()));

        tryCreate("Electric without pickups (should fail)", () ->
                electricFactory.create(
                        GuitarSpec.builder(GuitarType.ELECTRIC)
                                .bodyStyle(BodyStyle.STRATOCASTER)
                                .bodyWood(WoodType.ALDER)
                                .neckWood(WoodType.MAPLE)
                                .fretboardWood(WoodType.MAPLE)
                                .finish(new Finish("Red", Finish.Type.GLOSS))
                                .build()));
    }

    private static void printSection(String title) {
        System.out.println("\n── " + title + " " + "─".repeat(Math.max(0, 55 - title.length())));
    }

    private static void tryCreate(String label, Runnable action) {
        try {
            action.run();
            System.out.println("  [UNEXPECTED SUCCESS] " + label);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("  [OK] " + label + ": " + e.getMessage());
        }
    }
}
