package terreno;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JugadorTest {

    private ListaJugadores lista;

    @BeforeEach
    void setUp() {
        lista = ListaJugadores.getInstance();
        lista.lista.clear();
        lista.turnosDisponibles.clear();
        lista.indiceActual = 0;
        lista.ultimo_indice_lista = 0;
    }

    @Test
    void constructorInicializaValoresPorDefecto() {
        Jugador j = new Jugador(2, "VIDAL", "bot");
        assertEquals(100, j.getVida());
        assertEquals(10000, j.saldo);
        assertEquals("./img/tanque3.png", j.color);
        assertEquals("bot", j.tipo);
        assertTrue(j.activo);
        assertFalse(j.estaEliminado());
        assertEquals(0, j.getCantidad60() + j.getCantidad80() + j.getCantidad105());
    }

    @Test
    void ajustarVidaDescuentaDanio() {
        Jugador j = new Jugador(0, "MESSI", "jugador");
        assertEquals(70, j.ajustar_vida(j.getVida(), 30));
        assertEquals(70, j.getVida());
    }

    @Test
    void agregarSaldoSuma() {
        Jugador j = new Jugador(0, "MESSI", "jugador");
        j.agregar_saldo(2500);
        assertEquals(12500, j.saldo);
    }

    @Test
    void comprobarMunicionPorTipo() {
        lista.instanciarJugadores(2, 0);
        Jugador actual = lista.getJugadorActual();
        actual.setCantidad60(1);
        assertFalse(Jugador.comprobarMunicion(1, lista));
        assertTrue(Jugador.comprobarMunicion(2, lista));
        assertTrue(Jugador.comprobarMunicion(3, lista));
        assertFalse(Jugador.comprobarMunicion(99, lista));
    }

    @Test
    void tieneBalasYRevisarBalasDisponibles() {
        lista.instanciarJugadores(2, 0);
        assertFalse(Jugador.tiene_balas(lista));
        assertTrue(Jugador.revisarBalasDisponibles(lista));
        lista.lista.get(1).setCantidad105(2);
        assertFalse(Jugador.revisarBalasDisponibles(lista));
        assertFalse(Jugador.tiene_balas(lista)); // el actual (0) sigue sin balas
        lista.indiceActual = 1;
        assertTrue(Jugador.tiene_balas(lista));
    }

    @Test
    void pagarRondaSegunAsesinatosYSuicidios() {
        lista.instanciarJugadores(3, 0);
        lista.lista.get(0).asesinatos = 2;
        lista.lista.get(1).suicidios = 1;
        Jugador.pagar_ronda(lista);
        assertEquals(10000 + 10000 + 10000, lista.lista.get(0).saldo);
        assertEquals(10000 + 10000 - 5000, lista.lista.get(1).saldo);
        assertEquals(20000, lista.lista.get(2).saldo);
    }

    @Test
    void balaDescuentaMunicionSegunDanio() {
        Jugador j = new Jugador(0, "MESSI", "jugador");
        j.setCantidad60(3);
        j.setCantidad80(3);
        j.setCantidad105(3);
        new Bala(0, 0, 3, 45, 50, 0, 30, j);
        new Bala(0, 0, 3, 45, 50, 0, 40, j);
        new Bala(0, 0, 3, 45, 50, 0, 50, j);
        new Bala(0, 0, 3, 45, 50, 0, 50, j);
        assertEquals(2, j.getCantidad60());
        assertEquals(2, j.getCantidad80());
        assertEquals(1, j.getCantidad105());
    }

    @Test
    void balaCalculaPosicionYVelocidadInicial() {
        Jugador j = new Jugador(0, "MESSI", "jugador");
        Bala b = new Bala(100, 200, 3, 0, 50, 0, 30, j);
        assertEquals(140.0, b.ejeX, 1e-9); // distancia del cañon = 40
        assertEquals(200.0, b.ejeY, 1e-9);
        assertEquals(50.0, b.velocidadX, 1e-9);
        assertEquals(0.0, b.velocidadY, 1e-9);
        assertEquals(30, b.getDanio());
        assertFalse(b.eliminar());
        b.marcar();
        assertTrue(b.eliminar());

        Bala vertical = new Bala(0, 0, 3, 90, 10, 0, 40, j);
        assertEquals(0.0, vertical.velocidadX, 1e-9);
        assertEquals(10.0, vertical.velocidadY, 1e-9);
    }

    @Test
    void vientoDesactivadoDevuelveCero() {
        int previo = Globales.viento_def;
        Globales.viento_def = 0;
        try {
            assertEquals(0, Bala.cambiarViento(null));
        } finally {
            Globales.viento_def = previo;
        }
    }

    @Test
    void tanqueGuardaPosicion() {
        Tank t = new Tank("./img/tanque1.png", 0);
        t.agregarTanque(null, 0, 100, null, 120, 45);
        assertEquals(120, t.posicionX);
        assertEquals(45, t.getPosicionY());
    }
}
