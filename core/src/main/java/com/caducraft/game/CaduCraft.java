package com.caducraft.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class CaduCraft extends ApplicationAdapter {

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private ShapeRenderer miraRenderer;
    private SpriteBatch hudBatch;
    private BitmapFont fonte;
    private final Texture[] iconesItens = new Texture[TOTAL_TIPOS];
    private final TextureRegion[] regioesIconesItens = new TextureRegion[TOTAL_TIPOS];
    private Environment ambiente;
    private ColorAttribute luzAmbiente;
    private DirectionalLight luzSol;
    private float tempoCicloDiaNoite = 0f;

    // Sobrevivência: 20 pontos = 10 ícones.
    private int vida = 20;
    private int fome = 20;
    private float tempoFome = 0f;
    private float tempoRegeneracao = 0f;
    private float alturaQueda = 0f;
    private boolean caiu = false;
    private boolean jogadorVivo = true;
    private float spawnX, spawnY, spawnZ;
    private Texture vidaCheia, vidaVazia, vidaMeia;
    private Texture fomeCheia, fomeVazia, fomeMeia;

    private Model gramaModelo;
    private Model terraModelo;
    private Model pedraModelo;
    private Model troncoModelo;
    private Model folhasModelo;
    private Model tabuasModelo;
    private final Array<Texture> texturasBlocos = new Array<>();
    private Model bancadaModelo;
    private Model camaModelo;
    private Model rochaMatrizModelo;
    private float animacaoGolpeMao = 0f;
    private float tempoBalancoMao = 0f;

    private Array<ModelInstance> mundo;
    private final Array<CamaNoMundo> camasNoMundo = new Array<>();
    private final Array<ItemNoChao> itensNoChao = new Array<>();
    private final PilhaItem[] inventario = new PilhaItem[36];
    private boolean inventarioAberto = false;
    private int hotbarSelecionado = 0;
    private PilhaItem itemNaMao;
    private float progressoQuebra = 0f;
    private float tempoNecessarioQuebra = 1f;
    private int blocoAlvoX = -1;
    private int blocoAlvoY = -1;
    private int blocoAlvoZ = -1;
    private boolean menuCraftAberto = false;
    private boolean craftComBancada = false;
    private final PilhaItem[] gradeCraft = new PilhaItem[9];
    private static final int GRAMA = 0, TERRA = 1, PEDRA = 2, TRONCO = 3,
        FOLHAS = 4, TABUAS = 5, GRAVETOS = 6, MACHADO_MADEIRA = 7,
        PICARETA_MADEIRA = 8, BANCADA = 9, CAMA = 10;
    private static final int TOTAL_TIPOS = 11;
    private final String[] nomesInventario = {
        "Grama", "Terra", "Pedra", "Tronco", "Folhas",
        "Tabuas", "Gravetos", "Machado", "Picareta", "Bancada", "Cama"
    };
    private final Color[] coresInventario = {
        new Color(0.25f, 0.65f, 0.20f, 1f),
        new Color(0.48f, 0.29f, 0.15f, 1f),
        new Color(0.48f, 0.49f, 0.51f, 1f),
        new Color(0.36f, 0.21f, 0.10f, 1f),
        new Color(0.12f, 0.43f, 0.12f, 1f),
        new Color(0.62f, 0.39f, 0.20f, 1f),
        new Color(0.72f, 0.54f, 0.31f, 1f),
        new Color(0.52f, 0.35f, 0.18f, 1f),
        new Color(0.68f, 0.48f, 0.25f, 1f),
        new Color(0.42f, 0.28f, 0.16f, 1f),
        new Color(0.78f, 0.30f, 0.34f, 1f)
    };

    private static final int TAMANHO_SLOT = 42;
    private static final int ESPACO_SLOT = 4;

    private static class PilhaItem {
        int tipo;
        int quantidade;

        PilhaItem(int tipo, int quantidade) {
            this.tipo = tipo;
            this.quantidade = quantidade;
        }
    }

    private static class ItemNoChao {
        final int tipo;
        int quantidade;
        final ModelInstance instancia;
        final Vector3 posicao;
        final float alturaBase;
        float tempo = 0f;
        float rotacao = 0f;

        ItemNoChao(Model modelo, int tipo, float x, float y, float z) {
            this(modelo,tipo,x,y,z,1);
        }

        ItemNoChao(Model modelo,int tipo,float x,float y,float z,int quantidade) {
            this.tipo = tipo;
            this.quantidade = quantidade;
            this.posicao = new Vector3(x, y, z);
            this.alturaBase = y + 0.05f;
            this.instancia = new ModelInstance(modelo);
        }

        void atualizar(float delta) {
            tempo += delta;
            rotacao += 45f * delta;
            posicao.y = alturaBase + MathUtils.sin(tempo * 2f) * 0.05f;
            instancia.transform.setToTranslation(posicao)
                .rotate(Vector3.Y, rotacao)
                .scale(0.35f, 0.35f, 0.35f);
        }
    }

    private static class CamaNoMundo {
        final int x,y,z,dx,dz;
        final ModelInstance instancia;
        CamaNoMundo(Model modelo,int x,int y,int z,int dx,int dz) {
            this.x=x; this.y=y; this.z=z; this.dx=dx; this.dz=dz;
            this.instancia=new ModelInstance(modelo);
            float centroX=x+0.5f+dx*0.5f;
            float centroZ=z+0.5f+dz*0.5f;
            float rotacao=MathUtils.atan2(-dz,dx)*MathUtils.radiansToDegrees;
            this.instancia.transform.setToTranslation(centroX,y+0.5f,centroZ)
                .rotate(Vector3.Y,rotacao);
        }
        boolean ocupa(int bx,int by,int bz) {
            return by==y && ((bx==x && bz==z) || (bx==x+dx && bz==z+dz));
        }
    }

    private static final int TAMANHO = 64;
    private static final int MAX_ALTURA = 48;
    private static final int ALTURA_BASE_TERRENO = 9;
    private static final float DURACAO_CICLO = 1200f;

    // Colisão: cada posição indica se existe um bloco.
    private boolean[][][] solidos =
        new boolean[TAMANHO][MAX_ALTURA][TAMANHO];

    // Física do jogador
    private static final float VELOCIDADE_CAMINHADA = 4.317f;
    private static final float VELOCIDADE_CORRIDA = 5.612f;
    private float tempoDesdeToqueW = 1f;
    private boolean corridaDuploW = false;
    private float velocidadePulo = 0f;
    private float gravidade = -22f;

    private float alturaOlhos = 1.7f;
    private float alturaJogador = 1.8f;
    private float larguraJogador = 0.6f;

    private boolean noChao = false;

    // Câmera
    private float yaw = 0f;
    private float pitch = 0f;
    private float sensibilidade = 0.12f;

    @Override
    public void create() {

        mundo = new Array<>();
        modelBatch = new ModelBatch();
        miraRenderer = new ShapeRenderer();
        hudBatch = new SpriteBatch();
        fonte = new BitmapFont();
        carregarHUDSobrevivencia();
        Gdx.graphics.setForegroundFPS(60);

        criarCamera();
        criarIluminacao();
        criarModelos();
        criarIconesItens();
        gerarMundo();

        int centroMundo = TAMANHO / 2;
        float alturaInicial = alturaTerreno(centroMundo, centroMundo);

        camera.position.set(
            centroMundo + 0.5f,
            alturaInicial + alturaOlhos,
            centroMundo + 0.5f
        );
        spawnX = camera.position.x;
        spawnY = camera.position.y;
        spawnZ = camera.position.z;

        Gdx.input.setCursorCatched(true);

        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.R && !jogadorVivo) {
                    respawnar();
                } else if (keycode == Input.Keys.ESCAPE) {
                    if (menuCraftAberto) {
                        fecharMenuCraft();
                    } else if (inventarioAberto) {
                        if (devolverItemDaMao()) {
                            inventarioAberto = false;
                            Gdx.input.setCursorCatched(true);
                        }
                    } else {
                        Gdx.input.setCursorCatched(false);
                    }

                } else if (keycode == Input.Keys.E) {
                    if (menuCraftAberto) {
                        fecharMenuCraft();
                    } else if (!inventarioAberto) {
                        inventarioAberto = true;
                        Gdx.input.setCursorCatched(false);
                    } else if (devolverItemDaMao()) {
                        inventarioAberto = false;
                        Gdx.input.setCursorCatched(true);
                    }

                } else if (keycode == Input.Keys.C) {
                    if (menuCraftAberto) {
                        fecharMenuCraft();
                    } else {
                        abrirMenuCraft(false);
                    }

                } else if (keycode == Input.Keys.Q
                        && !inventarioAberto && !menuCraftAberto) {
                    droparItemSelecionado();

                } else if (keycode >= Input.Keys.NUM_1
                        && keycode <= Input.Keys.NUM_9
                        && !inventarioAberto && !menuCraftAberto) {
                    hotbarSelecionado = keycode - Input.Keys.NUM_1;
                }

                return true;
            }

            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {
                if (menuCraftAberto) {
                    clicarInterfaceCraft(x, y, button);
                    return true;
                }

                if (inventarioAberto) {
                    clicarInventario(x, y, button);
                    return true;
                }

                if (button == Input.Buttons.RIGHT) {
                    usarBotaoDireito();
                    return true;
                }

                Gdx.input.setCursorCatched(true);
                return true;
            }
        });

        atualizarCamera();
    }

    // =========================
    // SOBREVIVÊNCIA E HUD PNG
    // =========================
    private void carregarHUDSobrevivencia() {
        vidaCheia = carregarPNG("vida_cheia.png");
        vidaMeia = carregarPNG("vida_meia.png");
        vidaVazia = carregarPNG("vida_vazia.png");
        fomeCheia = carregarPNG("fome_cheia.png");
        fomeMeia = carregarPNG("fome_meia.png");
        fomeVazia = carregarPNG("fome_vazia.png");
    }

    private Texture carregarPNG(String arquivo) {
        if (!Gdx.files.internal(arquivo).exists()) {
            Gdx.app.error("CaduCraft", "PNG não encontrado em assets: " + arquivo);
            return null;
        }
        return new Texture(Gdx.files.internal(arquivo));
    }

    private void atualizarSobrevivencia(float delta) {
        if (!jogadorVivo) return;
        tempoFome += delta;
        if (tempoFome >= 30f) {
            tempoFome = 0f;
            fome = Math.max(0, fome - 1);
        }
        if (fome > 6 && vida < 20) {
            tempoRegeneracao += delta;
            if (tempoRegeneracao >= 5f) {
                vida = Math.min(20, vida + 1);
                tempoRegeneracao = 0f;
            }
        } else {
            tempoRegeneracao = 0f;
        }
        if (fome == 0) {
            tempoRegeneracao += delta;
            if (tempoRegeneracao >= 4f) {
                receberDano(1);
                tempoRegeneracao = 0f;
            }
        }
    }

    private void receberDano(int dano) {
        if (!jogadorVivo || dano <= 0) return;
        vida = Math.max(0, vida - dano);
        if (vida == 0) morrer();
    }

    private void morrer() {
        jogadorVivo = false;
        inventarioAberto = false;
        menuCraftAberto = false;
        itemNaMao = null;
        fonte.setColor(Color.WHITE);
    }

    private void respawnar() {
        camera.position.set(spawnX, spawnY, spawnZ);
        velocidadePulo = 0f;
        vida = 20;
        fome = 20;
        tempoFome = 0f;
        tempoRegeneracao = 0f;
        caiu = false;
        jogadorVivo = true;
        atualizarCamera();
    }

    private void desenharHUDSobrevivencia() {
        if (vidaCheia == null || vidaVazia == null || fomeCheia == null || fomeVazia == null) return;
        float escala = MathUtils.clamp(Gdx.graphics.getWidth() / 1100f, 0.65f, 1.1f);
        float tamanho = 22f * escala;
        float topo = Gdx.graphics.getHeight() - 34f * escala;
        float inicioVida = Gdx.graphics.getWidth() / 2f - 250f * escala;
        float inicioFome = Gdx.graphics.getWidth() / 2f + 35f * escala;
        hudBatch.begin();
        for (int i = 0; i < 10; i++) {
            int pontos = vida - i * 2;
            Texture iconeVida = pontos >= 2 ? vidaCheia : (pontos == 1 && vidaMeia != null ? vidaMeia : vidaVazia);
            if (iconeVida != null) hudBatch.draw(iconeVida, inicioVida + i * (tamanho + 2f), topo, tamanho, tamanho);
            int pontosFome = fome - i * 2;
            Texture iconeFome = pontosFome >= 2 ? fomeCheia : (pontosFome == 1 && fomeMeia != null ? fomeMeia : fomeVazia);
            if (iconeFome != null) hudBatch.draw(iconeFome, inicioFome + i * (tamanho + 2f), topo, tamanho, tamanho);
        }
        hudBatch.end();
    }

    private void desenharTelaMorte() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        miraRenderer.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        miraRenderer.begin(ShapeRenderer.ShapeType.Filled);
        miraRenderer.setColor(0f, 0f, 0f, 0.72f);
        miraRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        miraRenderer.end();
        hudBatch.begin();
        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(2f);
        fonte.draw(hudBatch, "VOCÊ MORREU", Gdx.graphics.getWidth()/2f - 100f, Gdx.graphics.getHeight()/2f + 25f);
        fonte.getData().setScale(1f);
        fonte.draw(hudBatch, "Pressione R para renascer", Gdx.graphics.getWidth()/2f - 105f, Gdx.graphics.getHeight()/2f - 20f);
        hudBatch.end();
    }

    // =========================
    // CÂMERA
    // =========================

    private void criarCamera() {

        camera = new PerspectiveCamera(
            75,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        camera.near = 0.1f;
        camera.far = 150f;

        atualizarCamera();
    }

    private void atualizarCamera() {

        float cosPitch = MathUtils.cosDeg(pitch);

        camera.direction.set(
            -MathUtils.sinDeg(yaw) * cosPitch,
            MathUtils.sinDeg(pitch),
            -MathUtils.cosDeg(yaw) * cosPitch
        ).nor();

        camera.up.set(Vector3.Y);

        camera.update();
    }

    private void controlarMouse() {

        if (!Gdx.input.isCursorCatched()) {
            return;
        }

        float dx = Gdx.input.getDeltaX();
        float dy = Gdx.input.getDeltaY();

        yaw -= dx * sensibilidade;
        pitch -= dy * sensibilidade;

        pitch = MathUtils.clamp(pitch, -85f, 85f);
    }

    // =========================
    // ILUMINAÇÃO
    // =========================

    private void criarIluminacao() {

        ambiente = new Environment();

        luzAmbiente = new ColorAttribute(
            ColorAttribute.AmbientLight,
            0.65f, 0.65f, 0.65f, 1f
        );
        ambiente.set(luzAmbiente);

        luzSol = new DirectionalLight().set(
            Color.WHITE,
            -1f, -0.8f, -0.3f
        );
        ambiente.add(luzSol);
    }

    // =========================
    // BLOCOS
    // =========================

    private void criarIconesItens() {
        for(int tipo=0;tipo<TOTAL_TIPOS;tipo++) {
            Pixmap imagem;
            if(tipo==MACHADO_MADEIRA) {
                imagem=new Pixmap(Gdx.files.internal("machado_madeira.png"));
            } else if(tipo==PICARETA_MADEIRA) {
                imagem=new Pixmap(Gdx.files.internal("picareta_madeira.png"));
            } else if(tipo==GRAMA) {
                imagem=new Pixmap(Gdx.files.internal("grama_icon.png"));
            } else if(tipo==TERRA) {
                imagem=new Pixmap(Gdx.files.internal("terra_icon.png"));
            } else if(tipo==PEDRA) {
                imagem=new Pixmap(Gdx.files.internal("pedra_icon.png"));
            } else if(tipo==TRONCO) {
                imagem=new Pixmap(Gdx.files.internal("tronco_icon.png"));
            } else if(tipo==FOLHAS) {
                imagem=new Pixmap(Gdx.files.internal("folhas_icon.png"));
            } else if(tipo==TABUAS) {
                imagem=new Pixmap(Gdx.files.internal("tabuas_icon.png"));
            } else if(tipo==BANCADA) {
                imagem=new Pixmap(Gdx.files.internal("bancada_icon.png"));
            } else {
                imagem=new Pixmap(32,32,Pixmap.Format.RGBA8888);
                imagem.setBlending(Pixmap.Blending.None);
                imagem.setColor(0f,0f,0f,0f);
                imagem.fill();
                if(tipo==GRAVETOS) desenharGravetos(imagem);
                else if(tipo==PICARETA_MADEIRA) desenharPicareta(imagem);
                else if(tipo==CAMA) desenharCamaIcone(imagem);
                else desenharBlocoIcone(imagem,tipo);
            }
            iconesItens[tipo]=new Texture(imagem);
            iconesItens[tipo].setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            regioesIconesItens[tipo]=new TextureRegion(iconesItens[tipo]);
            imagem.dispose();
        }
    }

    private void desenharBlocoIcone(Pixmap p,int tipo) {
        float topoR,topoG,topoB,esqR,esqG,esqB,dirR,dirG,dirB;
        if(tipo==GRAMA) {
            topoR=0.32f; topoG=0.78f; topoB=0.22f;
            esqR=0.48f; esqG=0.29f; esqB=0.15f;
            dirR=0.35f; dirG=0.20f; dirB=0.10f;
        } else {
            Color c=coresInventario[tipo];
            topoR=Math.min(1f,c.r*1.22f); topoG=Math.min(1f,c.g*1.22f); topoB=Math.min(1f,c.b*1.22f);
            esqR=c.r; esqG=c.g; esqB=c.b;
            dirR=c.r*0.72f; dirG=c.g*0.72f; dirB=c.b*0.72f;
        }
        p.setColor(topoR,topoG,topoB,1f);
        p.fillTriangle(16,2,30,9,16,16); p.fillTriangle(16,2,16,16,2,9);
        p.setColor(esqR,esqG,esqB,1f);
        p.fillTriangle(2,9,16,16,16,30); p.fillTriangle(2,9,16,30,2,22);
        p.setColor(dirR,dirG,dirB,1f);
        p.fillTriangle(16,16,30,9,30,22); p.fillTriangle(16,16,30,22,16,30);
        if(tipo==GRAMA) {
            p.setColor(0.28f,0.70f,0.19f,1f);
            p.fillTriangle(2,9,16,16,16,19); p.fillTriangle(2,9,16,19,2,13);
            p.fillTriangle(16,16,30,9,30,13); p.fillTriangle(16,16,30,13,16,19);
        } else if(tipo==PEDRA || tipo==TERRA) {
            p.setColor(tipo==PEDRA?0.62f:0.63f,tipo==PEDRA?0.63f:0.39f,tipo==PEDRA?0.64f:0.20f,1f);
            p.fillRectangle(5,15,3,2); p.fillRectangle(10,20,2,2); p.fillRectangle(5,23,3,2);
            p.fillRectangle(20,17,3,2); p.fillRectangle(24,12,2,2); p.fillRectangle(23,24,3,2);
        } else if(tipo==TRONCO) {
            p.setColor(0.63f,0.39f,0.20f,1f);
            p.drawLine(6,12,12,15); p.drawLine(7,16,12,19); p.drawLine(7,21,12,24);
            p.setColor(0.20f,0.11f,0.06f,1f);
            p.drawLine(20,17,27,13); p.drawLine(20,23,27,19); p.drawLine(20,28,26,25);
            p.setColor(0.75f,0.52f,0.28f,1f); p.fillCircle(16,9,3); p.fillCircle(16,9,1);
        } else if(tipo==FOLHAS) {
            p.setColor(0.08f,0.34f,0.08f,1f);
            p.fillRectangle(5,14,3,3); p.fillRectangle(11,21,2,2); p.fillRectangle(20,17,3,3);
            p.fillRectangle(24,12,2,2); p.fillRectangle(23,24,3,2);
        } else if(tipo==TABUAS) {
            p.setColor(0.31f,0.21f,0.12f,1f);
            // Em cima, juntas das tábuas; nas laterais, três fileiras horizontais.
            p.drawLine(7,7,16,12); p.drawLine(11,5,21,10);
            p.drawLine(3,15,14,20); p.drawLine(3,16,14,21);
            p.drawLine(3,20,14,25); p.drawLine(3,21,14,26);
            p.drawLine(18,20,28,15); p.drawLine(18,21,28,16);
            p.drawLine(18,25,28,20); p.drawLine(18,26,28,21);
        } else if(tipo==BANCADA) {
            // Grade de fabricação em cima e ferramentas simples nas laterais.
            p.setColor(0.29f,0.16f,0.08f,1f);
            p.fillRectangle(14,5,2,1);
            p.fillRectangle(11,7,2,1); p.fillRectangle(16,8,2,1); p.fillRectangle(20,7,2,1);
            p.fillRectangle(8,9,2,1); p.fillRectangle(14,10,2,1); p.fillRectangle(22,9,2,1);
            p.setColor(0.78f,0.52f,0.28f,1f);
            p.drawLine(7,18,12,21); p.drawLine(10,15,7,18);
            p.drawLine(21,18,26,16); p.drawLine(23,14,26,16);
            p.setColor(0.30f,0.17f,0.08f,1f);
            p.drawLine(16,20,16,26); p.drawLine(12,22,16,24); p.drawLine(16,24,20,22);
        }
        p.setColor(0.16f,0.10f,0.07f,1f);
        p.drawLine(16,2,30,9); p.drawLine(30,9,30,22); p.drawLine(30,22,16,30);
        p.drawLine(16,30,2,22); p.drawLine(2,22,2,9); p.drawLine(2,9,16,2);
        p.drawLine(2,9,16,16); p.drawLine(30,9,16,16); p.drawLine(16,16,16,30);
    }

    private void desenharCamaIcone(Pixmap p) {
        p.setColor(0.20f,0.10f,0.06f,1f); p.fillRectangle(3,10,26,15);
        p.setColor(0.47f,0.27f,0.12f,1f); p.fillRectangle(5,20,23,4);
        p.setColor(0.75f,0.18f,0.22f,1f); p.fillRectangle(5,12,22,8);
        p.setColor(0.96f,0.91f,0.78f,1f); p.fillRectangle(7,13,7,6);
        p.setColor(0.85f,0.35f,0.35f,1f); p.fillRectangle(16,13,10,6);
        p.setColor(0.15f,0.08f,0.04f,1f); p.drawLine(4,25,27,25); p.drawLine(4,10,4,24); p.drawLine(28,10,28,24);
    }

    private void desenharGravetos(Pixmap p) {
        p.setColor(0.28f,0.16f,0.08f,1f); p.drawLine(8,25,22,7); p.drawLine(9,26,23,8);
        p.setColor(0.72f,0.50f,0.27f,1f); p.drawLine(10,26,24,8); p.drawLine(13,28,27,10); p.drawLine(14,29,28,11);
    }

    private void desenharPicareta(Pixmap p) {
        // Silhueta pixelada: cabeça horizontal com duas pontas e cabo diagonal.
        p.setColor(0.38f,0.22f,0.11f,1f);
        p.fillRectangle(14,12,4,6); p.fillRectangle(16,17,4,6); p.fillRectangle(18,22,4,7);
        p.setColor(0.68f,0.48f,0.28f,1f);
        p.fillRectangle(2,8,8,5); p.fillRectangle(6,5,20,7); p.fillRectangle(22,8,8,5);
    }

    private void desenharIconeSprite(int tipo,float x,float y) {
        Texture textura=iconesItens[tipo];
        if(textura!=null) hudBatch.draw(textura,x+5f,y+5f,32f,32f);
    }
    private static final long ATRIBUTOS_TEXTURA_BLOCO = VertexAttributes.Usage.Position |
        VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

    private Texture carregarTexturaBloco(String arquivo) {
        Texture textura=new Texture(Gdx.files.internal(arquivo));
        textura.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        texturasBlocos.add(textura);
        return textura;
    }

    private Model criarModeloCuboTexturizado(String arquivo) {
        Texture textura=carregarTexturaBloco(arquivo);
        Material material=new Material(TextureAttribute.createDiffuse(textura),
            com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute.createAlphaTest(0.1f));
        return new ModelBuilder().createBox(1f,1f,1f,material,ATRIBUTOS_TEXTURA_BLOCO);
    }

    private void adicionarFaceTexturizada(MeshPartBuilder malha,float u0,float v0,float u1,float v1,
        float x1,float y1,float z1,float x2,float y2,float z2,
        float x3,float y3,float z3,float x4,float y4,float z4,float nx,float ny,float nz) {
        malha.setUVRange(u0,v0,u1,v1);
        malha.rect(x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4,nx,ny,nz);
    }

    // Atlas com uma imagem para os quatro lados e imagens próprias para topo e base.
    private Model criarModeloCuboComAtlas(String arquivo,
        float ladoU0,float ladoU1,float topoU0,float topoU1,float baseU0,float baseU1) {
        Texture textura=carregarTexturaBloco(arquivo);
        ModelBuilder builder=new ModelBuilder();
        builder.begin();
        MeshPartBuilder malha=builder.part("cubo",GL20.GL_TRIANGLES,ATRIBUTOS_TEXTURA_BLOCO,
            new Material(TextureAttribute.createDiffuse(textura),
                com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute.createAlphaTest(0.1f)));
        adicionarFaceTexturizada(malha,topoU0,0f,topoU1,1f,
            -.5f,.5f,-.5f,-.5f,.5f,.5f,.5f,.5f,.5f,.5f,.5f,-.5f,0f,1f,0f);
        adicionarFaceTexturizada(malha,baseU0,0f,baseU1,1f,
            -.5f,-.5f,.5f,-.5f,-.5f,-.5f,.5f,-.5f,-.5f,.5f,-.5f,.5f,0f,-1f,0f);
        adicionarFaceTexturizada(malha,ladoU0,0f,ladoU1,1f,
            .5f,-.5f,-.5f,-.5f,-.5f,-.5f,-.5f,.5f,-.5f,.5f,.5f,-.5f,0f,0f,-1f);
        adicionarFaceTexturizada(malha,ladoU0,0f,ladoU1,1f,
            -.5f,-.5f,.5f,.5f,-.5f,.5f,.5f,.5f,.5f,-.5f,.5f,.5f,0f,0f,1f);
        adicionarFaceTexturizada(malha,ladoU0,0f,ladoU1,1f,
            -.5f,-.5f,-.5f,-.5f,-.5f,.5f,-.5f,.5f,.5f,-.5f,.5f,-.5f,-1f,0f,0f);
        adicionarFaceTexturizada(malha,ladoU0,0f,ladoU1,1f,
            .5f,-.5f,.5f,.5f,-.5f,-.5f,.5f,.5f,-.5f,.5f,.5f,.5f,1f,0f,0f);
        return builder.end();
    }

    private Model criarModeloTronco() {
        return criarModeloCuboComAtlas("tronco_atlas.png",0f,0.5f,0.5f,1f,0.5f,1f);
    }

    private Model criarModeloGrama() {
        return criarModeloCuboComAtlas("grama_atlas.png",0f,1f/3f,1f/3f,2f/3f,2f/3f,1f);
    }

    private Model criarModeloTabuas() {
        return criarModeloCuboComAtlas("tabuas_atlas.png",0f,0.5f,0.5f,1f,0.5f,1f);
    }
    private Model criarModeloRochaMatriz() {
    Texture textura=carregarTexturaBloco("bedrock.png");
    textura.setWrap(Texture.TextureWrap.Repeat,Texture.TextureWrap.Repeat);
    ModelBuilder builder=new ModelBuilder();
    builder.begin();
    MeshPartBuilder malha=builder.part("rocha_matriz",GL20.GL_TRIANGLES,ATRIBUTOS_TEXTURA_BLOCO,
        new Material(TextureAttribute.createDiffuse(textura),
            com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute.createAlphaTest(0.1f)));
    float h=TAMANHO/2f;
    adicionarFaceTexturizada(malha,0f,0f,TAMANHO,TAMANHO,
        -h,0.5f,-h,-h,0.5f,h,h,0.5f,h,h,0.5f,-h,0f,1f,0f);
    adicionarFaceTexturizada(malha,0f,0f,TAMANHO,TAMANHO,
        -h,-0.5f,h,-h,-0.5f,-h,h,-0.5f,-h,h,-0.5f,h,0f,-1f,0f);
    adicionarFaceTexturizada(malha,0f,0f,TAMANHO,1f,
        h,-0.5f,-h,-h,-0.5f,-h,-h,0.5f,-h,h,0.5f,-h,0f,0f,-1f);
    adicionarFaceTexturizada(malha,0f,0f,TAMANHO,1f,
        -h,-0.5f,h,h,-0.5f,h,h,0.5f,h,-h,0.5f,h,0f,0f,1f);
    adicionarFaceTexturizada(malha,0f,0f,TAMANHO,1f,
        -h,-0.5f,-h,-h,-0.5f,h,-h,0.5f,h,-h,0.5f,-h,-1f,0f,0f);
    adicionarFaceTexturizada(malha,0f,0f,TAMANHO,1f,
        h,-0.5f,h,h,-0.5f,-h,h,0.5f,-h,h,0.5f,h,1f,0f,0f);
    return builder.end();
}


    private void criarModelos() {
        gramaModelo=criarModeloGrama();
        terraModelo=criarModeloCuboTexturizado("terra.png");
        pedraModelo=criarModeloCuboTexturizado("pedra.png");
        troncoModelo=criarModeloTronco();
        folhasModelo=criarModeloCuboTexturizado("folhas.png");
        tabuasModelo=criarModeloTabuas();
        bancadaModelo=criarBancadaModelo();
        camaModelo=criarCamaModelo();
        rochaMatrizModelo=criarModeloRochaMatriz();
    }



    private void animarMao() {
        animacaoGolpeMao=0.28f;
    }

    // A mão é um único quadrado simples; o item aparece separado sobre ela.
    private void renderizarMao(float delta) {
        if(animacaoGolpeMao>0f) animacaoGolpeMao=Math.max(0f,animacaoGolpeMao-delta);
        else if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) animarMao();
        tempoBalancoMao+=delta;
        boolean andando=Gdx.input.isKeyPressed(Input.Keys.W)||Gdx.input.isKeyPressed(Input.Keys.A)||
            Gdx.input.isKeyPressed(Input.Keys.S)||Gdx.input.isKeyPressed(Input.Keys.D);
        float escala=MathUtils.clamp(Gdx.graphics.getHeight()/900f,0.42f,1.35f);
        float balanco=andando ? MathUtils.sin(tempoBalancoMao*9f)*3f*escala : 0f;
        float progresso=1f-animacaoGolpeMao/0.28f;
        float golpe=animacaoGolpeMao>0f ? MathUtils.sin(progresso*MathUtils.PI) : 0f;
        float pulso=golpe*12f*escala;
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        miraRenderer.getProjectionMatrix().setToOrtho2D(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        miraRenderer.begin(ShapeRenderer.ShapeType.Filled);
        miraRenderer.setColor(0.90f,0.64f,0.43f,1f);
        float tamanhoMao=58f*escala;
        float maoX=Gdx.graphics.getWidth()-105f*escala;
        float maoY=8f*escala+balanco-pulso;
        miraRenderer.rect(maoX,maoY,tamanhoMao,tamanhoMao);
        miraRenderer.end();

        // O item fica sobre a mão para que a silhueta de picareta ou machado apareça inteira.
        PilhaItem item=inventario[hotbarSelecionado];
        if(item!=null && iconesItens[item.tipo]!=null) {
            boolean ferramenta=item.tipo==PICARETA_MADEIRA || item.tipo==MACHADO_MADEIRA || item.tipo==GRAVETOS;
            float tamanho=(ferramenta?116f:76f)*escala;
            float x=Gdx.graphics.getWidth()-205f*escala;
            float y=55f*escala+balanco-pulso;
            float angulo=ferramenta ? -22f+golpe*64f : -12f+golpe*28f;
            hudBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight()));
            hudBatch.setColor(Color.WHITE);
            hudBatch.begin();
            hudBatch.draw(regioesIconesItens[item.tipo],x,y,tamanho/2f,tamanho/2f,tamanho,tamanho,1f,1f,angulo);
            hudBatch.end();
        }
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }
    private Model criarBancadaModelo() {
        return criarModeloCuboComAtlas("bancada_atlas.png",0f,0.5f,0.5f,1f,0f,0.5f);
    }

    private Model criarCamaModelo() {
        long atributos=ATRIBUTOS_TEXTURA_BLOCO;
        ModelBuilder builder=new ModelBuilder();
        builder.begin();
        Texture madeira=carregarTexturaBloco("cama_madeira.png");
        MeshPartBuilder armação=builder.part("armacao",GL20.GL_TRIANGLES,atributos,
            new Material(TextureAttribute.createDiffuse(madeira)));
        armação.setVertexTransform(new Matrix4().setToTranslation(0f,-0.18f,0f));
        armação.box(1.92f,0.12f,0.90f);
        MeshPartBuilder pernas=builder.part("pernas",GL20.GL_TRIANGLES,atributos,
            new Material(TextureAttribute.createDiffuse(madeira)));
        for(float x:new float[]{-0.84f,0.84f}) for(float z:new float[]{-0.34f,0.34f}) {
            pernas.setVertexTransform(new Matrix4().setToTranslation(x,-0.34f,z));
            pernas.box(0.16f,0.32f,0.16f);
        }
        Texture tecido=carregarTexturaBloco("cama_colchao.png");
        MeshPartBuilder colchao=builder.part("colchao",GL20.GL_TRIANGLES,atributos,
            new Material(TextureAttribute.createDiffuse(tecido)));
        colchao.setVertexTransform(new Matrix4().setToTranslation(0f,-0.02f,0f));
        colchao.box(1.84f,0.22f,0.86f);
        Texture linho=carregarTexturaBloco("cama_travesseiro.png");
        MeshPartBuilder travesseiro=builder.part("travesseiro",GL20.GL_TRIANGLES,atributos,
            new Material(TextureAttribute.createDiffuse(linho)));
        travesseiro.setVertexTransform(new Matrix4().setToTranslation(0.62f,0.105f,0f));
        travesseiro.box(0.48f,0.08f,0.72f);
        return builder.end();
    }

    private void adicionarBloco(
        Model modelo,
        int x,
        int y,
        int z
    ) {

        if (x < 0 || x >= TAMANHO ||
            z < 0 || z >= TAMANHO ||
            y < 0 || y >= MAX_ALTURA) {
            return;
        }

        if (solidos[x][y][z]) return;

        ModelInstance bloco = new ModelInstance(modelo);

        // O bloco ocupa [x,x+1], [y,y+1], [z,z+1].
        bloco.transform.setToTranslation(
            x + 0.5f,
            y + 0.5f,
            z + 0.5f
        );

        mundo.add(bloco);

        // Registrar bloco para a colisão.
        solidos[x][y][z] = true;
    }

    // =========================
    // GERAÇÃO DO MUNDO
    // =========================

    private int alturaTerreno(int x, int z) {

        double onda1 = Math.sin(x * 0.25);
        double onda2 = Math.cos(z * 0.22);
        double onda3 = Math.sin((x + z) * 0.15);

        return Math.max(5, ALTURA_BASE_TERRENO + (int) Math.round(
            (onda1 + onda2 + onda3) * 1.4
        ));
    }

    private void adicionarBaseRochaMatriz() {
    ModelInstance base = new ModelInstance(rochaMatrizModelo);
    base.transform.setToTranslation(TAMANHO / 2f, 0.5f, TAMANHO / 2f);
    mundo.add(base);

    for (int x = 0; x < TAMANHO; x++) {
        for (int z = 0; z < TAMANHO; z++) {
            solidos[x][0][z] = true;
        }
    }
}


    private void gerarMundo() {

        // Terreno
        adicionarBaseRochaMatriz();
        for (int x = 0; x < TAMANHO; x++) {

            for (int z = 0; z < TAMANHO; z++) {

                int altura = alturaTerreno(x, z);

                for (int y = 1; y < altura; y++) {

                    Model modelo;

                    if (y == altura - 1) {
                        modelo = gramaModelo;
                    } else if (y >= altura - 4) {
                        modelo = terraModelo;
                    } else {
                        modelo = pedraModelo;
                    }

                    adicionarBloco(modelo, x, y, z);
                }
            }
        }

        gerarArvores();
    }

    private void gerarArvores() {

        Random random = new Random(12345);

        int quantidadeArvores = Math.max(22, TAMANHO * TAMANHO / 46);
        int centroMundo = TAMANHO / 2;
        for (int i = 0; i < quantidadeArvores; i++) {

            int x = 2 + random.nextInt(TAMANHO - 4);
            int z = 2 + random.nextInt(TAMANHO - 4);

            // Evitar árvores perto do jogador.
            if (Math.abs(x - centroMundo) < 4 &&
                Math.abs(z - centroMundo) < 4) {
                continue;
            }

            int altura = alturaTerreno(x, z);

            // Tronco
            for (int y = 0; y < 4; y++) {
                adicionarBloco(
                    troncoModelo,
                    x,
                    altura + y,
                    z
                );
            }

            // Folhas
            for (int dx = -2; dx <= 2; dx++) {

                for (int dz = -2; dz <= 2; dz++) {

                    for (int dy = 2; dy <= 4; dy++) {

                        if (Math.abs(dx) + Math.abs(dz) > 3) {
                            continue;
                        }

                        adicionarBloco(
                            folhasModelo,
                            x + dx,
                            altura + dy,
                            z + dz
                        );
                    }
                }
            }
        }
    }

    // =========================
    // INTERAÇÃO COM BLOCOS
    // =========================

    private static class AlvoBloco {
        final int x, y, z, anteriorX, anteriorY, anteriorZ;
        AlvoBloco(int x, int y, int z, int anteriorX, int anteriorY, int anteriorZ) {
            this.x=x; this.y=y; this.z=z;
            this.anteriorX=anteriorX; this.anteriorY=anteriorY; this.anteriorZ=anteriorZ;
        }
    }

    private AlvoBloco encontrarBlocoMirado() {
        Vector3 direcao = new Vector3(camera.direction).nor();
        Vector3 ponto = new Vector3(camera.position);
        int anteriorX = MathUtils.floor(ponto.x);
        int anteriorY = MathUtils.floor(ponto.y);
        int anteriorZ = MathUtils.floor(ponto.z);
        for (float distancia = 0f; distancia <= 6f; distancia += 0.04f) {
            ponto.set(camera.position).mulAdd(direcao, distancia);
            int x = MathUtils.floor(ponto.x);
            int y = MathUtils.floor(ponto.y);
            int z = MathUtils.floor(ponto.z);
            if (x < 0 || x >= TAMANHO || y < 0 || y >= MAX_ALTURA || z < 0 || z >= TAMANHO) return null;
            if (solidos[x][y][z]) return new AlvoBloco(x, y, z, anteriorX, anteriorY, anteriorZ);
            anteriorX = x; anteriorY = y; anteriorZ = z;
        }
        return null;
    }

    private void usarBotaoDireito() {
        animarMao();
        AlvoBloco alvo = encontrarBlocoMirado();
        if (alvo == null) return;
        int tipoMirado=tipoNoBloco(alvo.x,alvo.y,alvo.z);
        if (tipoMirado==BANCADA) {
            abrirMenuCraft(true);
            return;
        }
        if (tipoMirado==CAMA) {
            if (ehNoite()) pularNoite();
            return;
        }
        PilhaItem selecionado = inventario[hotbarSelecionado];
        if (selecionado == null) return;
        if (selecionado.tipo == CAMA) {
            if (colocarCama(alvo)) consumirItemSelecionado(selecionado);
            return;
        }
        Model modelo = modeloDoTipo(selecionado.tipo);
        if (modelo == null) return; // Ferramentas e gravetos não são blocos.
        int x = alvo.anteriorX, y = alvo.anteriorY, z = alvo.anteriorZ;
        if (x < 0 || x >= TAMANHO || y < 0 || y >= MAX_ALTURA || z < 0 || z >= TAMANHO) return;
        if (solidos[x][y][z] || colidindo(x + 0.5f, camera.position.y, z + 0.5f)) return;
        adicionarBloco(modelo, x, y, z);
        consumirItemSelecionado(selecionado);
    }

    private void consumirItemSelecionado(PilhaItem item) {
        item.quantidade--;
        if(item.quantidade<=0) inventario[hotbarSelecionado]=null;
    }

    private boolean colocarCama(AlvoBloco alvo) {
        int x=alvo.anteriorX, y=alvo.anteriorY, z=alvo.anteriorZ;
        if(x<0 || x>=TAMANHO || y<=0 || y>=MAX_ALTURA || z<0 || z>=TAMANHO) return false;
        int sinalX=camera.direction.x<0f ? -1 : 1;
        int sinalZ=camera.direction.z<0f ? -1 : 1;
        int[][] direcoes;
        // O comprimento da cama acompanha para onde o jogador está olhando.
        if(Math.abs(camera.direction.x)>Math.abs(camera.direction.z)) {
            direcoes=new int[][]{{sinalX,0},{-sinalX,0},{0,sinalZ},{0,-sinalZ}};
        } else {
            direcoes=new int[][]{{0,sinalZ},{0,-sinalZ},{sinalX,0},{-sinalX,0}};
        }
        for(int[] direcao:direcoes) {
            int dx=direcao[0], dz=direcao[1];
            int segundoX=x+dx, segundoZ=z+dz;
            if(segundoX<0 || segundoX>=TAMANHO || segundoZ<0 || segundoZ>=TAMANHO) continue;
            if(solidos[x][y][z] || solidos[segundoX][y][segundoZ]) continue;
            if(!solidos[x][y-1][z] || !solidos[segundoX][y-1][segundoZ]) continue;
            if(colidindo(x+0.5f,camera.position.y,z+0.5f) ||
                colidindo(segundoX+0.5f,camera.position.y,segundoZ+0.5f)) continue;
            camasNoMundo.add(new CamaNoMundo(camaModelo,x,y,z,dx,dz));
            solidos[x][y][z]=true;
            solidos[segundoX][y][segundoZ]=true;
            return true;
        }
        return false;
    }

    private void abrirMenuCraft(boolean comBancada) {
        menuCraftAberto = true;
        inventarioAberto = true;
        craftComBancada = comBancada;
        Gdx.input.setCursorCatched(false);
    }

    private void fecharMenuCraft() {
        if (!devolverItemDaMao()) return;
        int x = MathUtils.clamp(MathUtils.floor(camera.position.x), 0, TAMANHO - 1);
        int z = MathUtils.clamp(MathUtils.floor(camera.position.z), 0, TAMANHO - 1);
        for (int i = 0; i < gradeCraft.length; i++) {
            PilhaItem pilha = gradeCraft[i];
            if (pilha == null) continue;
            if (!adicionarItens(pilha.tipo, pilha.quantidade)) {
                Model modelo = modeloDoTipo(pilha.tipo);
                if (modelo != null) adicionarItemNoChao(modelo, pilha.tipo, x, Math.max(0, MathUtils.floor(camera.position.y)), z, pilha.quantidade);
            }
            gradeCraft[i] = null;
        }
        menuCraftAberto = false;
        inventarioAberto = false;
        craftComBancada = false;
        Gdx.input.setCursorCatched(true);
    }

    private int resultadoCraftTipo() {
        int ocupados=0, tipoUnico=-1;
        for (int i=0;i<gradeCraft.length;i++) if (gradeCraft[i]!=null) { ocupados++; tipoUnico=gradeCraft[i].tipo; }
        if (!craftComBancada && (gradeCraft[2]!=null || gradeCraft[5]!=null ||
            gradeCraft[6]!=null || gradeCraft[7]!=null || gradeCraft[8]!=null)) return -1;
        if (ocupados==1 && tipoUnico==TRONCO) return TABUAS;
        if (ocupados==2) {
            for (int col=0;col<3;col++) {
                int a=col, b=col+3;
                if (somenteNosSlots(new int[]{a,b}, TABUAS)) return GRAVETOS;
                a=col+3; b=col+6;
                if (somenteNosSlots(new int[]{a,b}, TABUAS)) return GRAVETOS;
            }
        }
        if (ocupados==4 && (somenteNosSlots(new int[]{0,1,3,4}, TABUAS) ||
            somenteNosSlots(new int[]{1,2,4,5}, TABUAS) ||
            somenteNosSlots(new int[]{3,4,6,7}, TABUAS) ||
            somenteNosSlots(new int[]{4,5,7,8}, TABUAS))) return BANCADA;
        if (craftComBancada && somenteNosSlots(new int[]{0,1,2,3,4,5}, -1)) {
            if (gradeCraft[0].tipo==FOLHAS && gradeCraft[1].tipo==FOLHAS && gradeCraft[2].tipo==FOLHAS &&
                gradeCraft[3].tipo==TABUAS && gradeCraft[4].tipo==TABUAS && gradeCraft[5].tipo==TABUAS) return CAMA;
        }
        if (craftComBancada && somenteNosSlots(new int[]{0,1,2,4,7}, -1)) {
            if (gradeCraft[0].tipo==TABUAS && gradeCraft[1].tipo==TABUAS && gradeCraft[2].tipo==TABUAS &&
                gradeCraft[4].tipo==GRAVETOS && gradeCraft[7].tipo==GRAVETOS) return PICARETA_MADEIRA;
        }
        if (craftComBancada && somenteNosSlots(new int[]{0,1,3,4,7}, -1)) {
            if (gradeCraft[0].tipo==TABUAS && gradeCraft[1].tipo==TABUAS && gradeCraft[3].tipo==TABUAS &&
                gradeCraft[4].tipo==GRAVETOS && gradeCraft[7].tipo==GRAVETOS) return MACHADO_MADEIRA;
        }
        if (craftComBancada && somenteNosSlots(new int[]{1,2,4,5,7}, -1)) {
            if (gradeCraft[1].tipo==TABUAS && gradeCraft[2].tipo==TABUAS && gradeCraft[5].tipo==TABUAS &&
                gradeCraft[4].tipo==GRAVETOS && gradeCraft[7].tipo==GRAVETOS) return MACHADO_MADEIRA;
        }
        return -1;
    }

    private boolean somenteNosSlots(int[] usados, int tipoEsperado) {
        boolean[] esperado = new boolean[9];
        for (int indice : usados) esperado[indice]=true;
        for (int i=0;i<9;i++) {
            if (esperado[i] != (gradeCraft[i] != null)) return false;
            if (esperado[i] && tipoEsperado >= 0 && gradeCraft[i].tipo != tipoEsperado) return false;
        }
        return true;
    }

    private int quantidadeResultadoCraft(int tipo) {
        if (tipo==TABUAS || tipo==GRAVETOS) return 4;
        return tipo < 0 ? 0 : 1;
    }

    private void fabricarDaGrade() {
        if (itemNaMao != null) return;
        int tipo=resultadoCraftTipo();
        int quantidade=quantidadeResultadoCraft(tipo);
        if (tipo<0 || !temEspacoPara(tipo, quantidade)) return;
        for (int i=0;i<gradeCraft.length;i++) if (gradeCraft[i]!=null) {
            gradeCraft[i].quantidade--;
            if (gradeCraft[i].quantidade<=0) gradeCraft[i]=null;
        }
        adicionarItens(tipo, quantidade);
    }

    private void clicarInterfaceCraft(int screenX, int screenY, int button) {
        float x=screenX, y=Gdx.graphics.getHeight()-screenY;
        int slot=craftSlotNaPosicao(x,y);
        if (slot>=0) { clicarPilha(gradeCraft, slot, button); return; }
        if (craftSaidaNaPosicao(x,y)) { if (button==Input.Buttons.LEFT) fabricarDaGrade(); return; }
        clicarInventario(screenX,screenY,button);
    }

    private int craftSlotNaPosicao(float x, float y) {
        float inicioX=Gdx.graphics.getWidth()/2f-150f;
        float inicioY=(Gdx.graphics.getHeight()-430f)/2f+220f;
        float passo=TAMANHO_SLOT+ESPACO_SLOT;
        for (int i=0;i<9;i++) {
            float sx=inicioX+(i%3)*passo;
            float sy=inicioY+(2-i/3)*passo;
            boolean permitido=craftComBancada || i==0 || i==1 || i==3 || i==4;
            if (permitido && x>=sx && x<=sx+TAMANHO_SLOT && y>=sy && y<=sy+TAMANHO_SLOT) return i;
        }
        return -1;
    }

    private float craftSaidaX() { return Gdx.graphics.getWidth()/2f+54f; }
    private float craftSaidaY() { return (Gdx.graphics.getHeight()-430f)/2f+220f+ESPACO_SLOT+TAMANHO_SLOT; }
    private boolean craftSaidaNaPosicao(float x,float y) {
        return x>=craftSaidaX() && x<=craftSaidaX()+TAMANHO_SLOT && y>=craftSaidaY() && y<=craftSaidaY()+TAMANHO_SLOT;
    }

    private void quebrarBlocoMirado(float delta) {
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            progressoQuebra = 0f;
            blocoAlvoX = blocoAlvoY = blocoAlvoZ = -1;
            return;
        }
        Vector3 direcao = new Vector3(camera.direction).nor();
        Vector3 ponto = new Vector3(camera.position);
        int anteriorX = Integer.MIN_VALUE;
        int anteriorY = Integer.MIN_VALUE;
        int anteriorZ = Integer.MIN_VALUE;

        // Passos curtos evitam atravessar blocos e limitam o alcance a 6 unidades.
        boolean encontrou = false;
        for (float distancia = 0; distancia <= 6f; distancia += 0.05f) {
            ponto.set(camera.position).mulAdd(direcao, distancia);

            int x = MathUtils.floor(ponto.x);
            int y = MathUtils.floor(ponto.y);
            int z = MathUtils.floor(ponto.z);

            if (x == anteriorX && y == anteriorY && z == anteriorZ) {
                continue;
            }
            anteriorX = x;
            anteriorY = y;
            anteriorZ = z;

            if (x < 0 || x >= TAMANHO || y < 0 || y >= MAX_ALTURA ||
                z < 0 || z >= TAMANHO) {
                return;
            }

            if (solidos[x][y][z]) {
                if (y == 0) {
    progressoQuebra = 0f;
    blocoAlvoX = blocoAlvoY = blocoAlvoZ = -1;
    return;
}

                encontrou = true;
                if (x != blocoAlvoX || y != blocoAlvoY || z != blocoAlvoZ) {
                    blocoAlvoX = x;
                    blocoAlvoY = y;
                    blocoAlvoZ = z;
                    progressoQuebra = 0f;
                    tempoNecessarioQuebra = tempoParaQuebrar(x, y, z);
                    animarMao();
                }
                break;
            }
        }

        if (!encontrou) {
            progressoQuebra = 0f;
            blocoAlvoX = blocoAlvoY = blocoAlvoZ = -1;
            return;
        }

        progressoQuebra += delta;
        if (progressoQuebra >= tempoNecessarioQuebra) {
            int ferramenta = inventario[hotbarSelecionado] == null
                ? -1 : inventario[hotbarSelecionado].tipo;
            int tipo = tipoNoBloco(blocoAlvoX, blocoAlvoY, blocoAlvoZ);
            boolean coletarDrop = tipo != PEDRA || ferramenta == PICARETA_MADEIRA;
            removerBloco(blocoAlvoX, blocoAlvoY, blocoAlvoZ, coletarDrop);
            progressoQuebra = 0f;
            blocoAlvoX = blocoAlvoY = blocoAlvoZ = -1;
        }
    }

    private float tempoParaQuebrar(int x, int y, int z) {
        int tipo=tipoNoBloco(x,y,z);
        int ferramenta=inventario[hotbarSelecionado]==null ? -1 : inventario[hotbarSelecionado].tipo;
        boolean machado=ferramenta==MACHADO_MADEIRA;
        boolean picareta=ferramenta==PICARETA_MADEIRA;

        if(tipo==PEDRA) return picareta ? 1.15f : 7.5f;
        if(tipo==BANCADA) return machado ? 1.9f : 3.75f;
        if(tipo==TRONCO || tipo==TABUAS) return machado ? 1.5f : 3f;
        if(tipo==GRAMA) return 0.9f;
        if(tipo==TERRA) return 0.75f;
        if(tipo==FOLHAS) return 0.35f;
        if(tipo==CAMA) return 0.3f;
        return 0.75f;
    }

    private int tipoNoBloco(int x, int y, int z) {
        for(CamaNoMundo cama:camasNoMundo) if(cama.ocupa(x,y,z)) return CAMA;
        for (ModelInstance bloco : mundo) {
            Vector3 centro = bloco.transform.getTranslation(new Vector3());
            if (MathUtils.floor(centro.x) == x &&
                MathUtils.floor(centro.y) == y &&
                MathUtils.floor(centro.z) == z) {
                return tipoDoBloco(bloco.model);
            }
        }
        return -1;
    }

    private void removerBloco(int x, int y, int z, boolean coletarDrop) {
        if (y == 0) return;
        for(int i=camasNoMundo.size-1;i>=0;i--) {
            CamaNoMundo cama=camasNoMundo.get(i);
            if(cama.ocupa(x,y,z)) {
                solidos[cama.x][cama.y][cama.z]=false;
                solidos[cama.x+cama.dx][cama.y][cama.z+cama.dz]=false;
                camasNoMundo.removeIndex(i);
                if(coletarDrop) adicionarItemNoChao(camaModelo,CAMA,x,y,z);
                return;
            }
        }
        for (int i = mundo.size - 1; i >= 0; i--) {
            ModelInstance bloco = mundo.get(i);
            Vector3 centro = bloco.transform.getTranslation(new Vector3());
            if (MathUtils.floor(centro.x) == x &&
                MathUtils.floor(centro.y) == y &&
                MathUtils.floor(centro.z) == z) {
                int tipo = tipoDoBloco(bloco.model);
                if (tipo >= 0 && coletarDrop) {
                    adicionarItemNoChao(bloco.model, tipo, x, y, z);
                }
                mundo.removeIndex(i);
                solidos[x][y][z] = false;
                return;
            }
        }
    }

    private void adicionarItemNoChao(Model modelo, int tipo, int x, int y, int z) {
        adicionarItemNoChao(modelo,tipo,x,y,z,1);
    }

    private void adicionarItemNoChao(Model modelo,int tipo,int x,int y,int z,int quantidade) {
        int alturaChao = 0;
        for (int blocoY = y - 1; blocoY >= 0; blocoY--) {
            if (solidos[x][blocoY][z]) {
                alturaChao = blocoY + 1;
                break;
            }
        }

        itensNoChao.add(new ItemNoChao(
            modelo, tipo, x + 0.5f, alturaChao + 0.2f, z + 0.5f, quantidade
        ));
    }

    private void atualizarItensNoChao(float delta, boolean recolher) {
        float pes = camera.position.y - alturaOlhos;
        float cabeca = pes + alturaJogador;

        for (int i = itensNoChao.size - 1; i >= 0; i--) {
            ItemNoChao item = itensNoChao.get(i);
            item.atualizar(delta);

            float dx = camera.position.x - item.posicao.x;
            float dz = camera.position.z - item.posicao.z;
            boolean pertoHorizontal = dx * dx + dz * dz <= 2.25f;
            boolean pertoVertical = item.posicao.y >= pes - 0.3f &&
                item.posicao.y <= cabeca + 0.3f;

            if (recolher && pertoHorizontal && pertoVertical &&
                adicionarItens(item.tipo, item.quantidade)) {
                itensNoChao.removeIndex(i);
            }
        }
    }

    private int limitePilha(int tipo) {
        return (tipo == MACHADO_MADEIRA || tipo == PICARETA_MADEIRA || tipo == CAMA) ? 1 : 64;
    }

    private boolean temEspacoPara(int tipo, int quantidade) {
        int disponivel=0, limite=limitePilha(tipo);
        for(PilhaItem pilha:inventario) {
            if(pilha==null) disponivel+=limite;
            else if(pilha.tipo==tipo) disponivel+=limite-pilha.quantidade;
        }
        return disponivel>=quantidade;
    }

    private boolean adicionarItens(int tipo,int quantidade) {
        if(!temEspacoPara(tipo,quantidade)) return false;
        int limite=limitePilha(tipo);
        for(int i=0;i<inventario.length;i++) {
            PilhaItem pilha=inventario[i];
            if(pilha!=null && pilha.tipo==tipo && pilha.quantidade<limite) {
                int adicionar=Math.min(quantidade,limite-pilha.quantidade);
                pilha.quantidade+=adicionar; quantidade-=adicionar;
                if(quantidade==0)return true;
            }
        }
        for(int i=0;i<inventario.length;i++) if(inventario[i]==null) {
            int adicionar=Math.min(quantidade,limite);
            inventario[i]=new PilhaItem(tipo,adicionar); quantidade-=adicionar;
            if(quantidade==0)return true;
        }
        return quantidade==0;
    }

    private void desenharMenuCraft() {
        float painelAltura=430f;
        float painelY=(Gdx.graphics.getHeight()-painelAltura)/2f;
        float painelX=(Gdx.graphics.getWidth()-500f)/2f;
        float gradeX=Gdx.graphics.getWidth()/2f-150f;
        float gradeY=painelY+220f;
        float passo=TAMANHO_SLOT+ESPACO_SLOT;
        int resultado=resultadoCraftTipo();
        int quantidade=quantidadeResultadoCraft(resultado);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        miraRenderer.getProjectionMatrix().setToOrtho2D(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        miraRenderer.begin(ShapeRenderer.ShapeType.Filled);
        miraRenderer.setColor(0f,0f,0f,0.55f);
        miraRenderer.rect(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        miraRenderer.setColor(0.12f,0.12f,0.12f,0.98f);
        miraRenderer.rect(painelX,painelY,500f,painelAltura);
        for(int i=0;i<9;i++) {
            float x=gradeX+(i%3)*passo;
            float y=gradeY+(2-i/3)*passo;
            boolean bloqueado=!craftComBancada && i!=0 && i!=1 && i!=3 && i!=4;
            desenharSlotCraft(gradeCraft[i],x,y,bloqueado);
        }
        float saidaX=craftSaidaX(), saidaY=craftSaidaY();
        desenharSlotCraft(resultado>=0 ? new PilhaItem(resultado,quantidade) : null,saidaX,saidaY,false);
        miraRenderer.end();
        miraRenderer.begin(ShapeRenderer.ShapeType.Line);
        miraRenderer.setColor(Color.LIGHT_GRAY);
        miraRenderer.rect(painelX,painelY,500f,painelAltura);
        miraRenderer.line(gradeX+3f*passo-4f,gradeY+passo+TAMANHO_SLOT/2f,saidaX-8f,gradeY+passo+TAMANHO_SLOT/2f);
        miraRenderer.line(saidaX-14f,gradeY+passo+TAMANHO_SLOT/2f+6f,saidaX-8f,gradeY+passo+TAMANHO_SLOT/2f);
        miraRenderer.line(saidaX-14f,gradeY+passo+TAMANHO_SLOT/2f-6f,saidaX-8f,gradeY+passo+TAMANHO_SLOT/2f);
        miraRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        hudBatch.begin();
        for(int i=0;i<9;i++) if(gradeCraft[i]!=null) {
            float x=gradeX+(i%3)*passo, y=gradeY+(2-i/3)*passo;
            desenharIconeSprite(gradeCraft[i].tipo,x,y);
        }
        if(resultado>=0) desenharIconeSprite(resultado,saidaX,saidaY);
        for(int i=0;i<9;i++) if(gradeCraft[i]!=null && gradeCraft[i].quantidade>1) {
            float x=gradeX+(i%3)*passo, y=gradeY+(2-i/3)*passo;
            fonte.setColor(Color.WHITE);
            fonte.draw(hudBatch,String.valueOf(gradeCraft[i].quantidade),x+TAMANHO_SLOT-14f,y+8f);
        }
        if(resultado>=0 && quantidade>1) {
            fonte.setColor(Color.WHITE);
            fonte.draw(hudBatch,String.valueOf(quantidade),saidaX+TAMANHO_SLOT-14f,saidaY+8f);
        }
        hudBatch.end();
    }

    private void desenharSlotCraft(PilhaItem pilha,float x,float y,boolean bloqueado) {
        miraRenderer.setColor(bloqueado ? 0.10f : 0.24f,bloqueado ? 0.10f : 0.24f,bloqueado ? 0.10f : 0.24f,1f);
        miraRenderer.rect(x,y,TAMANHO_SLOT,TAMANHO_SLOT);
        miraRenderer.setColor(bloqueado ? 0.04f : 0.07f,bloqueado ? 0.04f : 0.07f,bloqueado ? 0.04f : 0.07f,1f);
        miraRenderer.rect(x+3f,y+3f,TAMANHO_SLOT-6f,TAMANHO_SLOT-6f);
        if(pilha!=null) {

        }
    }

    private boolean devolverItemDaMao() {
        if (itemNaMao == null) return true;

        for (PilhaItem pilha : inventario) {
            int limite = limitePilha(itemNaMao.tipo);
            if (pilha != null && pilha.tipo == itemNaMao.tipo && pilha.quantidade < limite) {
                int transferir = Math.min(limite - pilha.quantidade, itemNaMao.quantidade);
                pilha.quantidade += transferir;
                itemNaMao.quantidade -= transferir;
                if (itemNaMao.quantidade == 0) {
                    itemNaMao = null;
                    return true;
                }
            }
        }

        for (int i = 0; i < inventario.length; i++) {
            if (inventario[i] == null) {
                inventario[i] = itemNaMao;
                itemNaMao = null;
                return true;
            }
        }
        return false;
    }

    private void clicarInventario(int screenX, int screenY, int button) {
        int slot=slotNaPosicao(screenX,Gdx.graphics.getHeight()-screenY);
        if (slot>=0) clicarPilha(inventario,slot,button);
    }

    private void clicarPilha(PilhaItem[] slots,int slot,int button) {
        PilhaItem conteudo=slots[slot];
        if (button==Input.Buttons.LEFT) {
            if (itemNaMao==null) { itemNaMao=conteudo; slots[slot]=null; }
            else if (conteudo==null) { slots[slot]=itemNaMao; itemNaMao=null; }
            else if (conteudo.tipo==itemNaMao.tipo && conteudo.quantidade<limitePilha(conteudo.tipo)) {
                int transferir=Math.min(limitePilha(conteudo.tipo)-conteudo.quantidade,itemNaMao.quantidade);
                conteudo.quantidade+=transferir; itemNaMao.quantidade-=transferir;
                if(itemNaMao.quantidade==0)itemNaMao=null;
            } else { slots[slot]=itemNaMao; itemNaMao=conteudo; }
        } else if(button==Input.Buttons.RIGHT) {
            if(itemNaMao==null && conteudo!=null) {
                int metade=(conteudo.quantidade+1)/2;
                itemNaMao=new PilhaItem(conteudo.tipo,metade); conteudo.quantidade-=metade;
                if(conteudo.quantidade==0)slots[slot]=null;
            } else if(itemNaMao!=null && conteudo==null) {
                slots[slot]=new PilhaItem(itemNaMao.tipo,1); itemNaMao.quantidade--;
                if(itemNaMao.quantidade==0)itemNaMao=null;
            } else if(itemNaMao!=null && conteudo!=null && conteudo.tipo==itemNaMao.tipo && conteudo.quantidade<limitePilha(conteudo.tipo)) {
                conteudo.quantidade++; itemNaMao.quantidade--;
                if(itemNaMao.quantidade==0)itemNaMao=null;
            }
        }
    }

    private int slotNaPosicao(float x, float y) {
        float larguraGrade = 9 * TAMANHO_SLOT + 8 * ESPACO_SLOT;
        float origemX = (Gdx.graphics.getWidth() - larguraGrade) / 2f;

        if (inventarioAberto) {
            float alturaPainel = menuCraftAberto ? 430f : 236f;
            float origemY = (Gdx.graphics.getHeight() - alturaPainel) / 2f;
            float hotbarY = origemY + 14f;
            int slot = slotNaGrade(x, y, origemX, hotbarY);
            if (slot >= 0) return slot;

            float gradeY = hotbarY + TAMANHO_SLOT + 14f;
            for (int linha = 0; linha < 3; linha++) {
                slot = slotNaGrade(x, y, origemX, gradeY +
                    linha * (TAMANHO_SLOT + ESPACO_SLOT));
                if (slot >= 0) return 27 - linha * 9 + slot;
            }
            return -1;
        }

        return slotNaGrade(x, y, origemX, 14f);
    }

    private int slotNaGrade(float x, float y, float origemX, float origemY) {
        float passo = TAMANHO_SLOT + ESPACO_SLOT;
        int coluna = (int) ((x - origemX) / passo);
        if (coluna < 0 || coluna >= 9) return -1;
        float xLocal = x - (origemX + coluna * passo);
        float yLocal = y - origemY;
        if (xLocal < 0 || xLocal > TAMANHO_SLOT ||
            yLocal < 0 || yLocal > TAMANHO_SLOT) {
            return -1;
        }
        return coluna;
    }

    private int tipoDoBloco(Model modelo) {
        if (modelo == gramaModelo) return GRAMA;
        if (modelo == terraModelo) return TERRA;
        if (modelo == pedraModelo) return PEDRA;
        if (modelo == troncoModelo) return TRONCO;
        if (modelo == folhasModelo) return FOLHAS;
        if (modelo == tabuasModelo) return TABUAS;
        if (modelo == bancadaModelo) return BANCADA;
        if (modelo == camaModelo) return CAMA;
        return -1;
    }

    private Model modeloDoTipo(int tipo) {
        if (tipo == GRAMA) return gramaModelo;
        if (tipo == TERRA) return terraModelo;
        if (tipo == PEDRA) return pedraModelo;
        if (tipo == TRONCO) return troncoModelo;
        if (tipo == FOLHAS) return folhasModelo;
        if (tipo == TABUAS) return tabuasModelo;
        if (tipo == BANCADA) return bancadaModelo;
        if (tipo == CAMA) return camaModelo;
        return null;
    }

    private void desenharMira() {
        float centroX = Gdx.graphics.getWidth() / 2f;
        float centroY = Gdx.graphics.getHeight() / 2f;

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        miraRenderer.getProjectionMatrix().setToOrtho2D(
            0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
        );
        miraRenderer.begin(ShapeRenderer.ShapeType.Line);
        miraRenderer.setColor(Color.WHITE);
        miraRenderer.rect(centroX - 1.5f, centroY - 8, 3f, 16f);
        miraRenderer.rect(centroX - 8, centroY - 1.5f, 16f, 3f);
        miraRenderer.end();
        if (progressoQuebra > 0f && tempoNecessarioQuebra > 0f) {
            float largura = 36f;
            float progresso = Math.min(1f, progressoQuebra / tempoNecessarioQuebra);
            miraRenderer.begin(ShapeRenderer.ShapeType.Filled);
            miraRenderer.setColor(0f, 0f, 0f, 0.8f);
            miraRenderer.rect(centroX - largura / 2f, centroY - 18f, largura, 4f);
            miraRenderer.setColor(Color.WHITE);
            miraRenderer.rect(centroX - largura / 2f, centroY - 18f,
                largura * progresso, 4f);
            miraRenderer.end();
        }
    }

    private void desenharInventario() {
        float larguraGrade = 9 * TAMANHO_SLOT + 8 * ESPACO_SLOT;
        float inicioX = (Gdx.graphics.getWidth() - larguraGrade) / 2f;
        float hotbarY = 14f;
        float gradeY = hotbarY + TAMANHO_SLOT + 14f;
        float origemPainelY = 0f;
        float alturaPainel = menuCraftAberto ? 430f : 236f;
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        miraRenderer.getProjectionMatrix().setToOrtho2D(
            0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
        );
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        miraRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (inventarioAberto) {
            float painelX = inicioX - 14f;
            origemPainelY = (Gdx.graphics.getHeight() - alturaPainel) / 2f;
            hotbarY = origemPainelY + 14f;
            gradeY = hotbarY + TAMANHO_SLOT + 14f;
            if (!menuCraftAberto) {
                miraRenderer.setColor(0f, 0f, 0f, 0.55f);
                miraRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                miraRenderer.setColor(0.10f, 0.10f, 0.10f, 0.96f);
                miraRenderer.rect(painelX, origemPainelY,
                    larguraGrade + 28f, alturaPainel);
            }
        }

        for (int i = 0; i < 9; i++) {
            float x = inicioX + i * (TAMANHO_SLOT + ESPACO_SLOT);
            desenharSlotPreenchido(i, x, hotbarY);
        }
        if (inventarioAberto) {
            for (int linha = 0; linha < 3; linha++) {
                int inicioIndice = 27 - linha * 9;
                float y = gradeY + linha * (TAMANHO_SLOT + ESPACO_SLOT);
                for (int coluna = 0; coluna < 9; coluna++) {
                    float x = inicioX + coluna * (TAMANHO_SLOT + ESPACO_SLOT);
                    desenharSlotPreenchido(inicioIndice + coluna, x, y);
                }
            }
        }
        if (inventarioAberto && itemNaMao != null) {
            float cursorX = Gdx.input.getX();
            float cursorY = Gdx.graphics.getHeight() - Gdx.input.getY();

        }
        miraRenderer.end();

        miraRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < 9; i++) {
            float x = inicioX + i * (TAMANHO_SLOT + ESPACO_SLOT);
            desenharBordaSlot(x, hotbarY, i == hotbarSelecionado);
        }
        if (inventarioAberto) {
            for (int linha = 0; linha < 3; linha++) {
                int inicioIndice = 27 - linha * 9;
                float y = gradeY + linha * (TAMANHO_SLOT + ESPACO_SLOT);
                for (int coluna = 0; coluna < 9; coluna++) {
                    float x = inicioX + coluna * (TAMANHO_SLOT + ESPACO_SLOT);
                    desenharBordaSlot(x, y, false);
                }
            }
        }
        miraRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        hudBatch.begin();
        for(int i=0;i<9;i++) if(inventario[i]!=null) {
            float x=inicioX+i*(TAMANHO_SLOT+ESPACO_SLOT);
            desenharIconeSprite(inventario[i].tipo,x,hotbarY);
        }
        if(inventarioAberto) for(int linha=0;linha<3;linha++) {
            int inicioIndice=27-linha*9; float y=gradeY+linha*(TAMANHO_SLOT+ESPACO_SLOT);
            for(int coluna=0;coluna<9;coluna++) { int indice=inicioIndice+coluna;
                if(inventario[indice]!=null) desenharIconeSprite(inventario[indice].tipo,inicioX+coluna*(TAMANHO_SLOT+ESPACO_SLOT),y);
            }
        }
        if(inventarioAberto && itemNaMao!=null) {
            float x=Gdx.input.getX()-16f, y=Gdx.graphics.getHeight()-Gdx.input.getY()-16f;
            desenharIconeSprite(itemNaMao.tipo,x,y);
        }
        fonte.setColor(Color.WHITE);

        for (int i = 0; i < 9; i++) {
            float x = inicioX + i * (TAMANHO_SLOT + ESPACO_SLOT);
            desenharQuantidade(i, x, hotbarY);
        }
        if (inventarioAberto) {
            for (int linha = 0; linha < 3; linha++) {
                int inicioIndice = 27 - linha * 9;
                float y = gradeY + linha * (TAMANHO_SLOT + ESPACO_SLOT);
                for (int coluna = 0; coluna < 9; coluna++) {
                    float x = inicioX + coluna * (TAMANHO_SLOT + ESPACO_SLOT);
                    desenharQuantidade(inicioIndice + coluna, x, y);
                }
            }
        }
        if (itemNaMao != null) {
            float mouseX = Gdx.input.getX() + 8f;
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY() - 8f;
            fonte.draw(hudBatch, "x " + itemNaMao.quantidade, mouseX, mouseY);
        }
        hudBatch.end();
    }


    private void desenharSlotPreenchido(int indice, float x, float y) {
        miraRenderer.setColor(0.22f, 0.22f, 0.22f, 1f);
        miraRenderer.rect(x, y, TAMANHO_SLOT, TAMANHO_SLOT);
        miraRenderer.setColor(0.08f, 0.08f, 0.08f, 1f);
        miraRenderer.rect(x + 3f, y + 3f, TAMANHO_SLOT - 6f, TAMANHO_SLOT - 6f);

        PilhaItem pilha = inventario[indice];
        if (pilha != null) {

        }
    }

    private void desenharBordaSlot(float x, float y, boolean selecionado) {
        miraRenderer.setColor(selecionado ? Color.WHITE : Color.LIGHT_GRAY);
        miraRenderer.rect(x, y, TAMANHO_SLOT, TAMANHO_SLOT);
    }

    private void desenharQuantidade(int indice, float x, float y) {
        PilhaItem pilha = inventario[indice];
        if (pilha != null && pilha.quantidade > 1) {
            fonte.draw(hudBatch, String.valueOf(pilha.quantidade),
                x + TAMANHO_SLOT - 14f, y + 9f);
        }
    }

    // =========================
    // SISTEMA DE COLISÃO
    // =========================

    private boolean colidindo(
        float x,
        float olhosY,
        float z
    ) {

        float raio = larguraJogador / 2f;

        // Posição dos pés e da cabeça.
        float pes = olhosY - alturaOlhos;
        float cabeca = pes + alturaJogador;

        int minX = MathUtils.floor(x - raio);
        int maxX = MathUtils.floor(x + raio - 0.001f);

        int minY = MathUtils.floor(pes);
        int maxY = MathUtils.floor(cabeca - 0.001f);

        int minZ = MathUtils.floor(z - raio);
        int maxZ = MathUtils.floor(z + raio - 0.001f);

        for (int bx = minX; bx <= maxX; bx++) {

            for (int by = minY; by <= maxY; by++) {

                for (int bz = minZ; bz <= maxZ; bz++) {

                    if (bx >= 0 && bx < TAMANHO &&
                        by >= 0 && by < MAX_ALTURA &&
                        bz >= 0 && bz < TAMANHO) {

                        if (solidos[bx][by][bz]) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // =========================
    // MOVIMENTAÇÃO
    // =========================

    private void movimentar(float delta) {
        if (!jogadorVivo) return;
        tempoDesdeToqueW += delta;
        boolean wPressionado = Gdx.input.isKeyPressed(Input.Keys.W);
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            corridaDuploW = tempoDesdeToqueW <= 0.30f;
            tempoDesdeToqueW = 0f;
        }
        if (!wPressionado) corridaDuploW = false;

        Vector3 frente = new Vector3(
            camera.direction.x,
            0,
            camera.direction.z
        ).nor();

        Vector3 direita = new Vector3(frente)
            .crs(Vector3.Y)
            .nor();

        float movimentoX = 0;
        float movimentoZ = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            movimentoX += frente.x;
            movimentoZ += frente.z;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            movimentoX -= frente.x;
            movimentoZ -= frente.z;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            movimentoX += direita.x;
            movimentoZ += direita.z;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            movimentoX -= direita.x;
            movimentoZ -= direita.z;
        }

        Vector3 movimento = new Vector3(
            movimentoX, 0, movimentoZ
        );

        if (movimento.len2() > 0) {

            boolean ctrlPressionado = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
            boolean correndo = wPressionado && (ctrlPressionado || corridaDuploW);
            float velocidade = correndo ? VELOCIDADE_CORRIDA : VELOCIDADE_CAMINHADA;
            movimento.nor().scl(velocidade * delta);

            // Movimento no eixo X
            tentarMover(
                camera.position.x + movimento.x,
                camera.position.y,
                camera.position.z
            );

            // Movimento no eixo Z
            tentarMover(
                camera.position.x,
                camera.position.y,
                camera.position.z + movimento.z
            );
        }

        // Pulo
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            && noChao) {

            velocidadePulo = 8f;
            noChao = false;
        }

        // Registra a maior altura antes de tocar o chão para calcular dano de queda.
        if (velocidadePulo > 0f || !noChao) {
            if (!caiu) alturaQueda = camera.position.y;
            caiu = true;
            alturaQueda = Math.max(alturaQueda, camera.position.y);
        }

        // Gravidade
        velocidadePulo += gravidade * delta;

        float novaAltura =
            camera.position.y + velocidadePulo * delta;

        if (!colidindo(
            camera.position.x,
            novaAltura,
            camera.position.z
        )) {

            camera.position.y = novaAltura;
            noChao = false;

        } else {

            if (velocidadePulo < 0) {
                noChao = true;
                if (caiu) {
                    float distanciaQueda = alturaQueda - camera.position.y;
                    if (distanciaQueda > 3f) {
                        receberDano(Math.max(1, (int)Math.floor(distanciaQueda - 3f)));
                    }
                    caiu = false;
                    alturaQueda = camera.position.y;
                }
            }

            velocidadePulo = 0;
        }

        // Limites do mundo
        float limite = larguraJogador / 2f;

        camera.position.x = MathUtils.clamp(
            camera.position.x,
            limite,
            TAMANHO - limite
        );

        camera.position.z = MathUtils.clamp(
            camera.position.z,
            limite,
            TAMANHO - limite
        );
    }

    private void tentarMover(
        float novoX,
        float atualY,
        float novoZ
    ) {

        float limite = larguraJogador / 2f;

        novoX = MathUtils.clamp(
            novoX, limite, TAMANHO - limite
        );

        novoZ = MathUtils.clamp(
            novoZ, limite, TAMANHO - limite
        );

        // Primeiro tenta andar normalmente.
        if (!colidindo(novoX, atualY, novoZ)) {

            if (Math.abs(novoX - camera.position.x) > 0) {
                camera.position.x = novoX;
            }

            if (Math.abs(novoZ - camera.position.z) > 0) {
                camera.position.z = novoZ;
            }

            return;
        }

        // Se houver um bloco de um nível, tenta subir.
        float alturaSubida = atualY + 1f;

        if (noChao &&
            !colidindo(novoX, alturaSubida, novoZ)) {

            camera.position.x = novoX;
            camera.position.z = novoZ;
            camera.position.y = alturaSubida;

            velocidadePulo = 0;
        }
    }
    private void droparItemSelecionado() {

    PilhaItem selecionado = inventario[hotbarSelecionado];

    if (selecionado == null) return;

    // Direção horizontal para onde o jogador está olhando.
    Vector3 frente = new Vector3(
        camera.direction.x,
        0,
        camera.direction.z
    ).nor();

    // Posição à frente do jogador.
    float x = camera.position.x + frente.x * 1.8f;
    float y = camera.position.y - alturaOlhos + 0.45f;
    float z = camera.position.z + frente.z * 1.8f;

    // Modelo visual do item.
    Model modelo = modeloDoTipo(selecionado.tipo);

    // Ferramentas e gravetos ainda não possuem modelo 3D próprio.
    if (modelo == null) {
        modelo = tabuasModelo;
    }

    // Criar o item no mundo.
    itensNoChao.add(new ItemNoChao(
        modelo,
        selecionado.tipo,
        x,
        y,
        z,
        1
    ));

    // Remover uma unidade do inventário.
    selecionado.quantidade--;

    if (selecionado.quantidade <= 0) {
        inventario[hotbarSelecionado] = null;
    }
}

    // =========================
    // EXECUÇÃO
    // =========================

    private boolean existeBloco(int x,int y,int z) {
        return x>=0 && x<TAMANHO && y>=0 && y<MAX_ALTURA && z>=0 && z<TAMANHO && solidos[x][y][z];
    }

    private boolean escondeFaceDeBloco(int x,int y,int z) {
        if(!existeBloco(x,y,z)) return false;
        // A cama ocupa a célula para colisão, mas não é um cubo inteiro que possa tampar a grama.
        for(CamaNoMundo cama:camasNoMundo) if(cama.ocupa(x,y,z)) return false;
        return true;
    }

    private boolean blocoVisivel(ModelInstance bloco) {
        float[] valores=bloco.transform.val;
        int x=MathUtils.floor(valores[Matrix4.M03]);
        int y=MathUtils.floor(valores[Matrix4.M13]);
        int z=MathUtils.floor(valores[Matrix4.M23]);
        return !escondeFaceDeBloco(x-1,y,z) || !escondeFaceDeBloco(x+1,y,z) ||
            !escondeFaceDeBloco(x,y-1,z) || !escondeFaceDeBloco(x,y+1,z) ||
            !escondeFaceDeBloco(x,y,z-1) || !escondeFaceDeBloco(x,y,z+1);
    }

    private void atualizarCicloDiaNoite(float delta) {
        tempoCicloDiaNoite += delta;
        if (tempoCicloDiaNoite >= DURACAO_CICLO) tempoCicloDiaNoite -= DURACAO_CICLO;
        float noite=quantidadeNoite();
        float dia=1f-noite;
        luzAmbiente.color.set(0.10f+0.55f*dia,0.12f+0.53f*dia,0.18f+0.47f*dia,1f);
        luzSol.set(0.14f+0.86f*dia,0.17f+0.83f*dia,0.25f+0.75f*dia,-1f,-0.8f,-0.3f);
    }

    private float quantidadeNoite() {
        // 10 minutos de dia, 50 segundos de entardecer,
        // 8 minutos e 20 segundos de noite, 50 segundos de amanhecer.
        if (tempoCicloDiaNoite < 600f) return 0f;
        if (tempoCicloDiaNoite < 650f) return (tempoCicloDiaNoite-600f)/50f;
        if (tempoCicloDiaNoite < 1150f) return 1f;
        return MathUtils.clamp(1f-(tempoCicloDiaNoite-1150f)/50f,0f,1f);
    }

    private boolean ehNoite() {
        return tempoCicloDiaNoite >= 650f;
    }

    private void pularNoite() {
        tempoCicloDiaNoite=0f;
        atualizarCicloDiaNoite(0f);
    }

    @Override
    public void render() {

        float delta = Math.min(
            Gdx.graphics.getDeltaTime(),
            0.05f
        );

        atualizarCicloDiaNoite(delta);
        atualizarSobrevivencia(delta);

        if (jogadorVivo && !inventarioAberto && !menuCraftAberto) {
            controlarMouse();
            movimentar(delta);
            quebrarBlocoMirado(delta);
        }
        atualizarItensNoChao(delta, !inventarioAberto && !menuCraftAberto);
        atualizarCamera();

        Gdx.gl.glViewport(
            0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        float noite=quantidadeNoite();
        float dia=1f-noite;
        Gdx.gl.glClearColor(
            0.015f+0.465f*dia, 0.02f+0.70f*dia, 0.07f+0.88f*dia, 1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT |
            GL20.GL_DEPTH_BUFFER_BIT
        );

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        modelBatch.begin(camera);

        for (ModelInstance bloco : mundo) {
            if (blocoVisivel(bloco)) {
                modelBatch.render(bloco, ambiente);
            }
        }
        for (CamaNoMundo cama : camasNoMundo) {
            modelBatch.render(cama.instancia, ambiente);
        }
        for (ItemNoChao item : itensNoChao) {
            modelBatch.render(item.instancia, ambiente);
        }

        modelBatch.end();
        if(!inventarioAberto && !menuCraftAberto) renderizarMao(delta);
        if (!inventarioAberto && !menuCraftAberto) {
            desenharMira();
        }
        if (menuCraftAberto) {
            desenharMenuCraft();
        }
        desenharInventario();
        if (jogadorVivo) desenharHUDSobrevivencia();
        else desenharTelaMorte();
    }

    @Override
    public void resize(int width, int height) {

        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void dispose() {

        modelBatch.dispose();
        miraRenderer.dispose();
        hudBatch.dispose();
        fonte.dispose();
        if (vidaCheia != null) vidaCheia.dispose();
        if (vidaMeia != null) vidaMeia.dispose();
        if (vidaVazia != null) vidaVazia.dispose();
        if (fomeCheia != null) fomeCheia.dispose();
        if (fomeMeia != null) fomeMeia.dispose();
        if (fomeVazia != null) fomeVazia.dispose();

        gramaModelo.dispose();
        terraModelo.dispose();
        pedraModelo.dispose();
        troncoModelo.dispose();
        folhasModelo.dispose();
        tabuasModelo.dispose();
        bancadaModelo.dispose();
        camaModelo.dispose();
        rochaMatrizModelo.dispose();
        for(Texture textura:iconesItens) if(textura!=null) textura.dispose();
        for(Texture textura:texturasBlocos) textura.dispose();
    }
}
