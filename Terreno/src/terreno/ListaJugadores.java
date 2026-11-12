    package terreno;

    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Iterator;

    public class ListaJugadores {
        private static ListaJugadores instance;
        private Terreno terreno;
        public int ultimo_indice_lista=0;
        public ArrayList<Jugador> lista= new ArrayList<>();

        public ArrayList<Integer> turnosDisponibles = new ArrayList<>();
        public int indiceActual; // se usa para acceder a jugador que esta en juego

        private ListaJugadores()
        {}

        // metodo singleton para la lista
        public static ListaJugadores getInstance() {
            if (instance == null) {
                instance = new ListaJugadores();
            }
            return instance;
        }

        public Terreno getTerreno() {
            return terreno;
        }

        public void setTerreno(Terreno terreno) {
            this.terreno = terreno;
        }

        public ArrayList<Jugador> getLista(){
            return lista;
        }
        //Metodo para iniciar los jugadore y los bots y agregarlos a la lista
        public void instanciarJugadores(int cantidadJugadores, int cantidadBots)
        {
            int cantidadNormales= cantidadJugadores-cantidadBots; //le quitamos la cantidad de bots a los jugadores
            String nombre="";
            //agregamos la cantidad de jguadores normales,
            for(int i=0;i<cantidadNormales;i++)
            {
                if(i==0){
                    nombre="MESSI";
                }
                if(i==1){
                    nombre="ALEXIS";
                }
                if(i==2){
                    nombre="VIDAL";
                }
                if(i==3){
                    nombre="SUAZO";
                }
                if(i==4){
                    nombre="YONE";
                }
                if(i==5){
                    nombre="YASUO";
                }
                Jugador aux = new Jugador(i,nombre,"jugador");
                lista.add(aux);
            }
            //agregamos la cantidad de jguadors que seran bots
            for(int i=cantidadNormales;i<cantidadJugadores;i++)
            {
                if(i==0){
                    nombre="MESSI";
                }
                if(i==1){
                    nombre="ALEXIS";
                }
                if(i==2){
                    nombre="VIDAL";
                }
                if(i==3){
                    nombre="SUAZO";
                }
                if(i==4){
                    nombre="YONE";
                }
                if(i==5){
                    nombre="YASUO";
                }

                Jugador aux= new Jugador(i,nombre,"bot");
                lista.add(aux);
            }
        }

        //funcion para mezclar la lista de turnos y crear una nueva cada turno
        public void generarTurnoAleatorio() {
            if (turnosDisponibles.isEmpty()) { // se revisa si la lista esta bacia, de ser asi se cren 2 sub listas donde se añaden los jugadores normales y los bots
                ArrayList<Integer> indicesNormales = new ArrayList<>();
                ArrayList<Integer> indicesBots = new ArrayList<>();
                for (Jugador jugador : lista) {
                    if (!jugador.estaEliminado()) {
                        if(jugador.activo){
                            if ("jugador".equals(jugador.tipo)) {
                                indicesNormales.add(jugador.jugador);
                            } else if ("bot".equals(jugador.tipo)) {
                                indicesBots.add(jugador.jugador);
                            }
                        }
                    }
                }
                //barajamos las ambas listas
                Collections.shuffle(indicesNormales);
                Collections.shuffle(indicesBots);

                turnosDisponibles.addAll(indicesNormales);
                turnosDisponibles.addAll(indicesBots);
                if(turnosDisponibles.size()>1)
                {
                 while(turnosDisponibles.get(0)==ultimo_indice_lista)
                 {
                     Collections.shuffle(turnosDisponibles);
                 }
                    ultimo_indice_lista = turnosDisponibles.get(turnosDisponibles.size()-1);
                }
                else
                {
                    Collections.shuffle(turnosDisponibles);
                }

                System.out.println("Lista de turnos creada -> " + turnosDisponibles);
            }
            if (!turnosDisponibles.isEmpty()) { // si la lista aun no esta vacia sacamos el primer elemento de  ella que representa el indice del jugador que esta jugando
                indiceActual = turnosDisponibles.remove(0);
                System.out.println("lista de turnos una vez actualizada ->"+turnosDisponibles);
                for(Jugador jugar : lista)
                {
                    System.out.println("jugador:"+jugar.jugador+"saldo"+jugar.saldo);
                }
            }

        }

        public Jugador getJugadorActual() {//toma el jugador actual en el flujo
            return lista.get(indiceActual);
        }

        public void eliminarJugador(int indiceJugador) {//elimina al jugador de la ronda
            if (indiceJugador < 0 || indiceJugador >= lista.size()) {
                //indice fuera de rango, no se hace nada
                return;
            }
            lista.get(indiceJugador).eliminar();
            Iterator<Integer> iterator = turnosDisponibles.iterator(); // si esta en la lista el jugador que fue eliminado lo eliminamos
            while (iterator.hasNext()) {
                if (iterator.next() == indiceJugador) {
                    iterator.remove();
                }
            }

        }
        
        public void desactivarJugador(int indiceJugador)//desactiva al jugador de la ronda
        {
            if (indiceJugador < 0 || indiceJugador >= lista.size()) {
                //indice fuera de rango, no se hace nada
                return;
            }
            lista.get(indiceJugador).descativar();

        }
        public int getCantidadVivos() {
            int vivos = 0;
            for (Jugador aux : lista) {
                if (!aux.estaEliminado()) {
                    vivos++;
                }
            }
            return vivos;
        }

        // 0 vivos = fin de ronda sin ganador (decisión mínima, revisar con el dueño)
        public Jugador getGanadorRonda() {
            Jugador ganador = null;
            for (Jugador aux : lista) {
                if (!aux.estaEliminado()) {
                    if (ganador != null) {
                        return null; // Hay más de 1 vivo: la ronda aún no termina con ganador
                    }
                    ganador = aux;
                }
            }
            return ganador; // Retorna el único vivo si queda 1, o null si quedan 0 vivos (sin ganador)
        }

        // 0 vivos = fin de ronda sin ganador (decisión mínima, revisar con el dueño)
        public boolean rondaTerminada() {
            if (lista.isEmpty()) {
                return false;
            }
            return getCantidadVivos() <= 1;
        }

        public boolean quedaUnoVivo() {
            // 0 vivos = fin de ronda sin ganador (decisión mínima, revisar con el dueño)
            return rondaTerminada();
        }
        
        public boolean quedanActivos()//si no queda ningun jugador activo(se quedaron todos sin balas) para finalizar la ronda
        {
            int cantidadMaximaDeMuertos=lista.size();
            int cantidad_inactivos=0;
            for(Jugador aux: lista)
            {
                if(!aux.activo)
                {
                    cantidad_inactivos++;
                }

            }
            if(cantidad_inactivos==cantidadMaximaDeMuertos)
            {
                return true;
            }
            return false;
        }
        
        //se reviven todos los jugadores que murieron en la ronda y se reinicia todo
        //para dejarlos listos para el reinicio de partido completo 
        public void revivir_jugadores()
        {
            for (Jugador aux : lista)
            {
                aux.eliminado=false; // no esta eliminado
                aux.activo=true; // lo marcamos como activo
                //reinicio de atributos
                aux.vida=100;
                aux.suicidios=0;
                aux.saldo=10000;
                aux.asesinatos=0;
                aux.cantidad80=0;
                aux.cantidad105=0;
                aux.cantidad60=0;
                aux.asesinatosTotales=0;
                aux.suicidiosTotales=0;

            }
        }
        
        public void revivir()//similar al metodo anterior, solo que este reinicia los valores de los jugadores
                             //para una siguiente ronda, no para reiniciar un partido completo
        {
            for (Jugador aux : lista)
            {
                aux.eliminado=false; // no esta eliminado
                aux.activo=true; // lo marcamos como activo
                //reinicio de vida
                aux.vida=100;              
            }
        }


    }