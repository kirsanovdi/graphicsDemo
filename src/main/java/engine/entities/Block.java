package engine.entities;

import org.joml.Vector3i;

import java.util.Arrays;

/**
 * Блок, основной элемент при создании сцены
 */
public class Block {

    public Vector3i cord;
    public final int id;

    public final int[] sideIds;
    public final boolean[] sideRender;

    /**
     * Конструктор блока
     *
     * @param cord    координата блока
     * @param id      id блока
     * @param sideIds массив[6] id сторон
     */
    public Block(Vector3i cord, int id, int[] sideIds) {
        if (sideIds.length != 6)
            throw new IllegalArgumentException("Массив id сторон блока неравен стандартному размеру(6)");
        this.cord = cord;
        this.id = id;
        this.sideIds = sideIds;
        this.sideRender = new boolean[]{true, true, true, true, true, true};
    }

    /**
     * Перегруженный метод toString, используется при сохранении сцены
     *
     * @return текстовое представление блока
     */
    @Override
    public String toString() {
        return "Block: cord=[\t" + cord.x + '\t' + cord.y + '\t' + cord.z + "\t],\tid=" + id +
                ",\tsideIds=" + Arrays.toString(sideIds);
    }
}
