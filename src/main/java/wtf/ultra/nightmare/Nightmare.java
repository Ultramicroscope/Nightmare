package wtf.ultra.nightmare;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL13.GL_SAMPLE_ALPHA_TO_COVERAGE;

@Mod(modid = Nightmare.MODID, version = Nightmare.VERSION)
public class Nightmare {
    public static final String MODID = "nightmare";
    public static final String VERSION = "1.0";

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final int displayList;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        double eyeHeight = mc.thePlayer.getEyeHeight();
        double reach = mc.playerController.getBlockReachDistance();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();

        GlStateManager.shadeModel(GL_SMOOTH);
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE);

        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        
        glTranslated(0, eyeHeight, 0);
        glScaled(reach, reach, reach);
        glCallList(displayList);

        glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE);
        glDisable(GL_MULTISAMPLE);
        glDisable(GL_LINE_SMOOTH);

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    static {
        int res = 128;

        double[][][][] vertexArr = new double[res + 1][res][4][3];
        double rseg = 2 * Math.PI / (double) res;
        for (int i = 0; i <= res; i++) {
            double lat0 = Math.PI * ((i - 1) / (double) res - 0.5);
            double z0 = Math.sin(lat0);
            double zr0 = Math.cos(lat0);

            double lat1 = Math.PI * (i / (double) res - 0.5);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);

            for (int j = 0; j < res; j++) {
                double lng0 = rseg * j;
                double x0 = Math.cos(lng0);
                double y0 = Math.sin(lng0);

                double lng1 = rseg * (j + 1);
                double x1 = Math.cos(lng1);
                double y1 = Math.sin(lng1);

                vertexArr[i][j][0][0] = x0 * zr0;
                vertexArr[i][j][0][1] = y0 * zr0;
                vertexArr[i][j][0][2] = z0;

                vertexArr[i][j][1][0] = x0 * zr1;
                vertexArr[i][j][1][1] = y0 * zr1;
                vertexArr[i][j][1][2] = z1;

                vertexArr[i][j][2][0] = x1 * zr1;
                vertexArr[i][j][2][1] = y1 * zr1;
                vertexArr[i][j][2][2] = z1;

                vertexArr[i][j][3][0] = x1 * zr0;
                vertexArr[i][j][3][1] = y1 * zr0;
                vertexArr[i][j][3][2] = z0;
            }
        }

        displayList = GLAllocation.generateDisplayLists(1);
        glNewList(displayList, GL_COMPILE);
            glColor3d(0, 0, 0);
            glBegin(GL_QUADS);
            for (int i = 0; i < res + 1; i++)
                for (int j = 0; j < res; j++)
                    for (int k = 0; k < 4; k++)
                        glVertex3d(vertexArr[i][j][k][0], vertexArr[i][j][k][1], vertexArr[i][j][k][2]);
            glEnd();
        glEndList();
    }
}
