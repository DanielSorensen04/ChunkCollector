package me.dalot.serializing;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ItemSerializer {

    public static String toBase64(ArrayList<ItemStack> items) {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.size());

            for (int i = 0; i < items.size(); i++) {
                dataOutput.writeObject(items.get(i));
            }

            dataOutput.flush();

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Ikke i stand til at gemme item stacks.", e);
        }
    }

    public static ArrayList<ItemStack> fromBase64(String data) throws IOException {

        ArrayList<ItemStack> items = new ArrayList<>();

        if (data.equalsIgnoreCase(" ")) {
            return items;
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int size = dataInput.readInt();

            for (int i = 0; i < size; i++) {
                items.add(i, (ItemStack) dataInput.readObject());
            }
            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Ikke i stand til at decode class type.", e);
        }
    }
}
