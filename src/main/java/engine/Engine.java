package engine;

import controller.Commands;
import controller.Controller;
import controller.Mode;
import engine.entities.Block;
import engine.entities.LightPoint;
import engine.entities.Line;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Engine {
    private final Controller controller;

    public final ConcurrentHashMap<Vector3i, Block> blocks;
    public final Set<Line> lines;
    public final Set<LightPoint> lightPoints;

    public Engine(Controller controller, Block[] initBlocks) {
        System.out.println("Инициализация EngineRuntime");
        this.controller = controller;

        blocks = new ConcurrentHashMap<>();
        setBlocks(initBlocks);
        lines = new HashSet<>();
        lightPoints = new HashSet<>();
        lightPoints.add(new LightPoint(new Vector3f(0.0f, 7.0f, -10.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)));
        lightPoints.add(new LightPoint(new Vector3f(0.0f, 7.0f, 10.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
        System.out.println("Инициализация EngineRuntime завершена");
    }

    private void setBlocks(Block[] initBlocks) {
        for (Block initBlock : initBlocks) {
            if (initBlock == null) throw new RuntimeException("initBlock was null");
            blocks.put(initBlock.cord, initBlock);
            updateBlockSpace(initBlock.cord);
        }
    }

    public static Block[] generateBlockLayer(Vector3i start, int delta) {
        final Block[] result = new Block[delta * delta];
        for (int z = 0; z < delta; z++) {
            for (int x = 0; x < delta; x++) {
                final int sideId = 0;
                result[z * delta + x] = new Block(new Vector3i(start.x + x - delta / 2, start.y, start.z + z - delta / 2), 0, new int[]{sideId, sideId, sideId, sideId, sideId, sideId});
            }
        }
        return result;
    }

    public boolean checkCord(Vector3i vector3i) {
        return blocks.containsKey(vector3i) && blocks.get(vector3i).id != -1;
    }

    private void pairUpdate(Block block, Vector3i anotherCord, int sideR, int sideAR) {
        if (checkCord(anotherCord)) {
            block.sideRender[sideR] = false;
            blocks.get(anotherCord).sideRender[sideAR] = false;
        } else {
            block.sideRender[sideR] = true;
        }
    }

    private void pairDeleteUpdate(Vector3i anotherCord, int sideAR) {
        if (checkCord(anotherCord)) blocks.get(anotherCord).sideRender[sideAR] = true;
    }

    private void updateBlockSpace(Vector3i vector3i) {
        final Vector3i center = vector3i;
        if (blocks.containsKey(vector3i)) {
            Block block = blocks.get(vector3i);
            pairUpdate(block, new Vector3i(center).add(0, 0, 1), 0, 3);
            pairUpdate(block, new Vector3i(center).add(0, 0, -1), 3, 0);
            pairUpdate(block, new Vector3i(center).add(0, 1, 0), 1, 2);
            pairUpdate(block, new Vector3i(center).add(0, -1, 0), 2, 1);
            pairUpdate(block, new Vector3i(center).add(1, 0, 0), 5, 4);
            pairUpdate(block, new Vector3i(center).add(-1, 0, 0), 4, 5);
        } else {
            pairDeleteUpdate(new Vector3i(center).add(0, 0, 1), 3);
            pairDeleteUpdate(new Vector3i(center).add(0, 0, -1), 0);
            pairDeleteUpdate(new Vector3i(center).add(0, 1, 0), 2);
            pairDeleteUpdate(new Vector3i(center).add(0, -1, 0), 1);
            pairDeleteUpdate(new Vector3i(center).add(1, 0, 0), 4);
            pairDeleteUpdate(new Vector3i(center).add(-1, 0, 0), 5);
        }
    }

    protected static Vector3i getVector3i(Vector3f v3f) {
        return new Vector3i((int) v3f.x + (v3f.x < 0 ? -1 : 0), (int) v3f.y + (v3f.y < 0 ? -1 : 0), (int) v3f.z + (v3f.z < 0 ? -1 : 0));
    }

    public void rayTrace(Vector3f orientation, Vector3f position, Commands command) {
        final Vector3i startPosI = getVector3i(position);
        final Vector3f dir = new Vector3f(orientation).div(10000);


        Vector3i lastCheckedPos = null;
        Vector3i lastCord = null;
        Vector3i selectedCord = null;
        Vector3f selectedFloatCord = null;

        for (int i = 0; i < 100 * 10000; i++) {
            position.add(dir);
            Vector3i posI = getVector3i(position);
            if (checkCord(posI)) {
                if (lastCheckedPos != null && !lastCheckedPos.equals(startPosI)) {
                    lastCord = lastCheckedPos;
                    selectedCord = posI;
                    selectedFloatCord = position;
                }
                break;
            }
            lastCheckedPos = posI;
        }
        int sideId = 1;
        if(command == Commands.ADD && lastCord != null) {
            blocks.put(lastCord, new Block(lastCord, 0, new int[]{sideId, sideId, sideId, sideId, sideId, sideId}));
            updateBlockSpace(lastCord);
        }
        if(command == Commands.REMOVE && selectedCord != null) {
            blocks.remove(selectedCord);
            updateBlockSpace(selectedCord);
        }
    }

    public void run() {
        System.out.println("EngineRuntime started");
        while (controller.status == Mode.RUNNING) {

            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("EngineRuntime finished");
    }
}
