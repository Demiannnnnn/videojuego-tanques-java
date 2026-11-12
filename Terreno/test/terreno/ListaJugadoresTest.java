package terreno;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListaJugadoresTest {

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
    void esSingleton() {
        assertSame(lista, ListaJugadores.getInstance());
    }

    @Test
    void instanciarJugadoresCreaNormalesYBots() {
        lista.instanciarJugadores(4, 2);
        assertEquals(4, lista.getLista().size());
        assertEquals("MESSI", lista.lista.get(0).nombre);
        assertEquals("jugador", lista.lista.get(1).tipo);
        assertEquals("VIDAL", lista.lista.get(2).nombre);
        assertEquals("bot", lista.lista.get(2).tipo);
        assertEquals("bot", lista.lista.get(3).tipo);
        for (int i = 0; i < 4; i++) {
            assertEquals(i, lista.lista.get(i).jugador);
        }
    }

    @Test
    void turnosRecorrenATodosLosJugadoresUnaVezPorRonda() {
        lista.instanciarJugadores(4, 1);
        Set<Integer> vistos = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            lista.generarTurnoAleatorio();
            vistos.add(lista.indiceActual);
        }
        assertEquals(Set.of(0, 1, 2, 3), vistos);
        assertTrue(lista.turnosDisponibles.isEmpty());
    }

    @Test
    void jugadoresHumanosJueganAntesQueBots() {
        lista.instanciarJugadores(4, 2);
        lista.ultimo_indice_lista = -1;
        lista.generarTurnoAleatorio();
        assertTrue(lista.indiceActual < 2);
        lista.generarTurnoAleatorio();
        assertTrue(lista.indiceActual < 2);
    }

    @Test
    void elPrimerTurnoNoRepiteAlUltimoJugador() {
        lista.instanciarJugadores(3, 0);
        for (int ronda = 0; ronda < 20; ronda++) {
            int ultimo = lista.ultimo_indice_lista;
            lista.generarTurnoAleatorio();
            assertNotEquals(ultimo, lista.indiceActual);
            lista.turnosDisponibles.clear();
        }
    }

    @Test
    void eliminarJugadorLoSacaDeLosTurnos() {
        lista.instanciarJugadores(3, 0);
        lista.turnosDisponibles.addAll(java.util.List.of(0, 1, 2));
        lista.eliminarJugador(1);
        assertTrue(lista.lista.get(1).estaEliminado());
        assertFalse(lista.turnosDisponibles.contains(1));
        assertEquals(2, lista.turnosDisponibles.size());
        lista.eliminarJugador(-1);
        lista.eliminarJugador(10);
        assertEquals(2, lista.turnosDisponibles.size());
    }

    @Test
    void eliminadosEInactivosNoRecibenTurno() {
        lista.instanciarJugadores(3, 0);
        lista.eliminarJugador(0);
        lista.desactivarJugador(2);
        lista.ultimo_indice_lista = -1;
        lista.generarTurnoAleatorio();
        assertEquals(1, lista.indiceActual);
        assertTrue(lista.turnosDisponibles.isEmpty());
    }

    @Test
    void quedaUnoVivo() {
        lista.instanciarJugadores(3, 0);
        assertFalse(lista.quedaUnoVivo());
        lista.eliminarJugador(0);
        assertFalse(lista.quedaUnoVivo());
        lista.eliminarJugador(2);
        assertTrue(lista.quedaUnoVivo());
    }

    @Test
    void dosVivosNoTerminaRonda() {
        lista.instanciarJugadores(3, 0);
        lista.eliminarJugador(0); // Quedan 2 vivos
        assertFalse(lista.quedaUnoVivo());
        assertFalse(lista.rondaTerminada());
        assertNull(lista.getGanadorRonda());
    }

    @Test
    void unVivoTerminaRondaConGanador() {
        lista.instanciarJugadores(3, 0);
        lista.eliminarJugador(0);
        lista.eliminarJugador(1); // Queda exactamente 1 vivo (índice 2: VIDAL)
        assertTrue(lista.quedaUnoVivo());
        assertTrue(lista.rondaTerminada());
        Jugador ganador = lista.getGanadorRonda();
        assertNotNull(ganador);
        assertEquals(2, ganador.jugador);
        assertEquals("VIDAL", ganador.nombre);
    }

    @Test
    void ceroVivosTerminaRondaSinGanador() {
        lista.instanciarJugadores(3, 0);
        lista.eliminarJugador(0);
        lista.eliminarJugador(1);
        lista.eliminarJugador(2); // 0 vivos (muertos todos en la misma explosión)
        assertTrue(lista.quedaUnoVivo(), "Con 0 vivos la ronda debe terminar");
        assertTrue(lista.rondaTerminada(), "Con 0 vivos rondaTerminada debe ser true");
        assertNull(lista.getGanadorRonda(), "Con 0 vivos no debe haber ganador");
    }

    @Test
    void cierreDeRondaConCeroVivosNoLanzaExcepcion() {
        lista.instanciarJugadores(3, 0);
        lista.eliminarJugador(0);
        lista.eliminarJugador(1);
        lista.eliminarJugador(2); // 0 vivos
        assertDoesNotThrow(() -> {
            Jugador ganador = Jugar.cerrarRondaLogica(lista);
            assertNull(ganador, "Con 0 vivos no debe asignarse ganador");
        });
        // Comprobar que los jugadores se reviven para la siguiente ronda
        for (Jugador j : lista.getLista()) {
            assertFalse(j.estaEliminado());
            assertEquals(100, j.getVida());
        }
    }

    @Test
    void cierreDeRondaConUnVivoRetornaGanador() {
        lista.instanciarJugadores(3, 0);
        lista.eliminarJugador(0);
        lista.eliminarJugador(1); // Queda 1 vivo (índice 2)
        assertDoesNotThrow(() -> {
            Jugador ganador = Jugar.cerrarRondaLogica(lista);
            assertNotNull(ganador);
            assertEquals(2, ganador.jugador);
        });
    }

    @Test
    void cierreDeRondaSeguroConListaVaciaONula() {
        assertDoesNotThrow(() -> {
            assertNull(Jugar.cerrarRondaLogica(null));
            lista.lista.clear();
            assertNull(Jugar.cerrarRondaLogica(lista));
        });
    }

    @Test
    void quedanActivosCuandoTodosSinBalas() {
        lista.instanciarJugadores(2, 0);
        assertFalse(lista.quedanActivos());
        lista.desactivarJugador(0);
        assertFalse(lista.quedanActivos());
        lista.desactivarJugador(1);
        assertTrue(lista.quedanActivos());
    }

    @Test
    void revivirSoloRestauraVidaYEstado() {
        lista.instanciarJugadores(2, 0);
        Jugador j = lista.lista.get(0);
        j.ajustar_vida(100, 80);
        j.eliminar();
        j.descativar();
        j.saldo = 1;
        j.asesinatos = 3;
        lista.revivir();
        assertEquals(100, j.getVida());
        assertTrue(j.activo);
        assertFalse(j.estaEliminado());
        assertEquals(1, j.saldo);
        assertEquals(3, j.asesinatos);
    }

    @Test
    void revivirJugadoresReiniciaPartidaCompleta() {
        lista.instanciarJugadores(2, 0);
        Jugador j = lista.lista.get(1);
        j.saldo = 1;
        j.asesinatos = 3;
        j.suicidiosTotales = 2;
        j.setCantidad80(4);
        j.eliminar();
        lista.revivir_jugadores();
        assertEquals(10000, j.saldo);
        assertEquals(0, j.asesinatos);
        assertEquals(0, j.suicidiosTotales);
        assertEquals(0, j.getCantidad80());
        assertFalse(j.estaEliminado());
    }
}
