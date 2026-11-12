package terreno;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Jugar  {     
    static Random random = new Random();//random para el terreno
    public boolean jugando=true;//estado actual del juego, si es false se finaliza todo
    double angulo;//angulo del disparo
    double velocidad;//velocidad del disparo
    double cañonX;//posicion del angulo del cañon X
    double cañonY;//posicion del angulo del cañon Y
    int impacto;//colision con un tanque
    int distancia=0;//distancia alcanzada de bala
    int altura=0;//altura alcanzada de bala
    final int pixel = 3;//dimension del pixel de cada elemento que lo usa
    int validar=0;//usada para diferenciar si el terreno se reinicia manualmente o por proceso de cambio de turno(boton reiniciar)   
    int tipo=0;//tipo de bala seleccionada
    double deltaTiempo = 0.09;//tiempo de fluidez del disparo
    int viento;//cantidad de viento si es que esta habilitado
    
    ListaJugadores listJugador;//lista que contiene a todos los jugadores del partido y toda su informacion
    int alto = (Globales.alto_resolucion/pixel);//largo de terreno e interfaz
    int ancho=(Globales.ancho_resolucion/pixel);//ancho de terreno e interfaz
    Interfaz interfaz=new Interfaz(Globales.alto_resolucion,Globales.ancho_resolucion);
      
    int contador_inicio=0;//revisa si es la caida inicial de los tanques, ya que en esta no se deben hacer damage
    int vidatanque=100;//vida que se va actualizando y enviando al reinstanciar los tanques al impacto
    private boolean disparo_en_curso = false;//revisa si no hay una bala en curso
    
    public Jugar(ListaJugadores listJugador) {
        this.listJugador = listJugador;
        
    }
          
    private static int terreno_random;//variable que guarda la seleccion random del terreno
    
    static{
        terreno_random =random.nextInt(3);
    }
    Terreno terrain = new Terreno(Globales.alto_resolucion/pixel,Globales.ancho_resolucion/pixel, pixel,interfaz.gc);
    Tienda escenaTienda = new Tienda();//tienda de este partido
    Clasificacion resultados= new Clasificacion();//tabla de este partido
    

   
    public void start() {
        Musica.agregar_musica_terreno();//musica
        if(revisarEstado()){//revisa si se sigue jugando
            return;
        }
        viento=Bala.cambiarViento(interfaz);//generamos el primer viento
        Globales.stage.setResizable(true);
        //define la gravedad
        if(Globales.gravedad_def==1){
            Globales.gravedad=-15.81;
        }
        if(Globales.gravedad_def==2){
            Globales.gravedad=-5.81;
        }

        if(Globales.rondas_def==0){
            Globales.stage.close();
        }
        interfaz.iniciar_interfaz();
        //muestra los valores en la interfaz
        interfaz.cantidadRondas.setText(Integer.toString(Globales.rondas_def));
        interfaz.cantidadGravedad.setText(Double.toString(Globales.gravedad*-1));
        if(Globales.viento_def==0){
            interfaz.cantidadViento.setText("Nulo");
        }
        
        definirPosicion();//define posicion inicial
        iniciar_terreno();//inicia matriz
        //muestra al primer jugador
        interfaz.mostrarJugador(listJugador.getJugadorActual());     

        interfaz.finalizar.setOnAction(event -> {//se apreta finalizar y se termina la ejecucion                
            finalizarJuego();
        });
        interfaz.reiniciar.setOnAction(event -> {//se realiza todo el proceso para reiniciar la partida
            Musica.detenerMusica();
            reiniciar_partida();
        });        
        tipo=0;//reiniciamos el tipo para que no permita disparar la bala anterior sin antes escogerla
        elegir_bala();
        
        interfaz.estadisticas(listJugador);
        interfaz.disparar.setOnAction(event ->{
            Musica.sonido_click();
            if (Jugador.comprobarMunicion(tipo,listJugador)) {//verifica si quedan balas del tipo seleccionado
                //muestra una ventana emergente
                HBox aviso=VentanaEmergente.aparecer("¡No quedan balas  \n     de este tipo!",3);
                interfaz.canvasPane.getChildren().add(aviso);
                aviso.setLayoutX(Globales.alto_resolucion/2-70);
                aviso.setLayoutY(Globales.ancho_resolucion/2);
                //revisa si le quedan balas al intentar disparar
                if(Jugador.comprobarMunicion(1,listJugador)&&Jugador.comprobarMunicion(2,listJugador)&&Jugador.comprobarMunicion(3,listJugador)){
                    aviso=VentanaEmergente.aparecer("Jugador se quedo sin balas....",3);
                    interfaz.canvasPane.getChildren().add(aviso);
                    aviso.setLayoutX(Globales.alto_resolucion/2-120);
                    aviso.setLayoutY(0);
                    Timeline delay = new Timeline(new KeyFrame(Duration.seconds(3), e -> animacionCaida()));
                    delay.play();
                    return;
                }
                tipo=0;                   
                if(Jugador.revisarBalasDisponibles(listJugador)){//si se acaban las balas de todos, se finaliza la ronda
                    aviso=VentanaEmergente.aparecer("Finalizando Ronda...",3);
                    interfaz.canvasPane.getChildren().add(aviso);
                    aviso.setLayoutX(Globales.alto_resolucion/2-80);
                    aviso.setLayoutY(0);
                    Timeline delay = new Timeline(new KeyFrame(Duration.seconds(3), e -> finalizarRonda()));
                    delay.play();
                }                                               
            }              
            if(tipo==0){//si no se selecciono nada o se equivoco
                return;
            }
            interfaz.ingresar_disparo();//ingresa los label que guardaran el valor de angulo y velocidad
            String anguloTexto = interfaz.entradaangulo.getText();
            String velocidadTexto = interfaz.entradavelocidad.getText();       
            if(anguloTexto.isEmpty() || velocidadTexto.isEmpty() || anguloTexto.equals("0") || velocidadTexto.equals("0")){//verifica las entradas             
                interfaz.disparar.setDisable(false);
                return;
            }
            angulo = -Double.parseDouble(anguloTexto);
            velocidad = Double.parseDouble(velocidadTexto);
            Bala nuevaBala = crear_bala();//creamos nueva bala con el tipo seleciconado       
            Musica.sonido_disparo();//sonido
            disparo_en_curso = false;
            animacionBala(nuevaBala);//trayectoria bala
         
        //reiniciamos los valores de los textfield
        interfaz.entradaangulo.setText("");
        interfaz.entradavelocidad.setText("");
        }           
        );      
    } 
    
    public void iniciar_terreno(){//inicializa la matriz del terreno y la dibuja dependiendo de la eleccion random
        if(revisarEstado()){
            return;
        }
        terrain.iniciar();       
        validar=0;
        if(terreno_random == 0) {
            terrain.terreno_nieve(interfaz.gc, 0.0, 100,validar, terrain,alto,ancho);
            animacionCaida();
        }
        if(terreno_random == 1) {
            terrain.terreno_desierto(interfaz.gc, 0.0, 100,validar,terrain,alto,ancho);
            animacionCaida();
        }
        if(terreno_random == 2) {
            terrain.terreno_aram(interfaz.gc, 0.0, 100,validar,terrain,alto,ancho);
            animacionCaida();
        }
        Globales.stage.show();       
    }   
    
    //METODO QUE REALIZA TODOS LOS IMPACTOS A UN JUGADOR, SEA POR EXPLOSIN, CAIDA, O DISPARO DIRECTO
    //Y DEFINE SI FUE ASESINATO O SUICIDIO
    public void impacto_jugador(int jugadorImpactado, int danio) {
        if(revisarEstado()){
            return;
        }
        jugadorImpactado--;// Ajustar para acceder al índice
        Jugador jugador = listJugador.lista.get(jugadorImpactado);
        jugador.ajustar_vida(jugador.getVida(), danio);
        //verifica si el jugador actual es diferente del jugador impactado
        boolean es_el_mismo=false;
        if (listJugador.getJugadorActual() != jugador) {
            if (jugador.vida <= 0) {
                listJugador.getJugadorActual().asesinatos++;
                listJugador.getJugadorActual().asesinatosTotales++;
            }
        } 
        else {
            es_el_mismo=true;
        }
        if (jugador.vida <= 0) {
            terrain.borrarHitboxJugador(jugadorImpactado + 1);//valor de representacion en la hitbox del jugador
            if(!es_el_mismo){
                listJugador.eliminarJugador(jugadorImpactado); // Marca al jugador como eliminado
                HBox muerte=VentanaEmergente.aparecer("Jugador "+jugador.nombre+" muerto...", 3);
                interfaz.canvasPane.getChildren().add(muerte);
                muerte.setLayoutX(Globales.alto_resolucion-300);
                muerte.setLayoutY(0);
            }
            else{//si no es el mismo
                listJugador.getJugadorActual().suicidios++;// le agregamos un suicidio
                listJugador.getJugadorActual().suicidiosTotales++;
                HBox muerte=VentanaEmergente.aparecer("Jugador "+jugador.nombre+" se a suicidado..."+"contador de suicidios->"+jugador.suicidios, 3);
                interfaz.canvasPane.getChildren().add(muerte);
                muerte.setLayoutX(Globales.alto_resolucion-300);
                muerte.setLayoutY(0);
                listJugador.eliminarJugador(jugadorImpactado);
            }
            // 0 vivos = fin de ronda sin ganador (decisión mínima, revisar con el dueño)
            if (listJugador.quedaUnoVivo()) {//Si queda un jugador, termina y reinicia
                finalizarRonda();
            }
        }       
    }
   
    public void colision_bala(){//METODO LLAMADO CUANDO LA BALA COLISIONA CON CUALQUIER COSA
        Musica.sonido_colision();       
        if(revisarEstado()){
            return;
        }
        calcular_explosion();
        if(terreno_random == 0) {
            terrain.terreno_nieve(interfaz.gc, 0.0, vidatanque,validar,terrain,alto,ancho);
        }
        if(terreno_random == 1) {
            terrain.terreno_desierto(interfaz.gc, 0.0, vidatanque,validar,terrain,alto,ancho);
        }
        if(terreno_random == 2) {
            terrain.terreno_aram(interfaz.gc, 0.0, vidatanque,validar,terrain,alto,ancho);
        }   
        tipo=0;
    } 
    
    public Bala crear_bala(){//CREA BALA DEPENDIENDO DEL TIPO ACTUAL
        if(revisarEstado()){
            return null;
        }
        int danio=0;
        if(tipo==1){
            danio=30;
        }
        if(tipo==2){
            danio=40;
        }
        if(tipo==3){
            danio=50;
        }
        cañonX = listJugador.getJugadorActual().getTanque().getCañonX();
        cañonY = listJugador.getJugadorActual().getTanque().getCañonY();
        Bala nuevaBala = new Bala((int) cañonX, (int) cañonY, pixel , angulo, velocidad,0,danio,listJugador.getJugadorActual());
        return nuevaBala;
    }

    public void calcular_explosion(){//calcula la explosion en base a un area que se genera con el contacto entre la explosion y la hitbox del tanque(para efectos mas realistas)
        if(revisarEstado()){
            return;
        }
        int area=0;
        int tanque=0;
        int choque;
        for(int i=0;i<terrain.matriz.length;i++){
            for(int j=0;j<terrain.matriz[i].length;j++){
                choque=terrain.matriz[i][j];
                if(terrain.explosion[i][j]==1 && choque==2){
                    area++;
                    tanque=1;           
                }
                if(terrain.explosion[i][j]==1 && choque==3){
                    area++;
                    tanque=2;
                }
                if(terrain.explosion[i][j]==1 && choque==4){
                    area++;
                    tanque=3;
                }
                if(terrain.explosion[i][j]==1 && choque==5){
                    area++;
                    tanque=4;
                }
                if(terrain.explosion[i][j]==1 && choque==6){
                    area++;
                    tanque=5;
                }
                if(terrain.explosion[i][j]==1 && choque==7){
                    area++;
                    tanque=6;
                }
            }
        }
        System.out.println("el area es "+area);
        area = Math.round(area / 2);//se divide a la mitad ya que la hitbox del tanque tiene 200 valores, y la vida del tanque es 100, para hacerlo mas preciso
        if(area>terrain.radio){//en caso de que el area sea mayor al daño propio de la bala, se inflinge el daño base de la bala partido a la mitad, ya que no fue un disparo directo como tal
            if(terrain.radio==5){ 
                area=30/2;
            }
            if(terrain.radio==10){ 
                area=40/2;
            }
            if(terrain.radio==15){ 
                area=50/2;
            }    
        }
        if(tanque!=0){
            impacto_jugador(tanque,area);//realiza el impacto al tanque
            System.out.println("daño de area");
        }
        terrain.reiniciar_matriz(terrain.explosion);//reinicia la matriz de explosion para que asi no guarde valores de explosiones anteriores     
    }
    
    //METODO QUE SE ENCARGA DE TODO EL PROCESO MIENTRAS LA BALA TIENE TRAYECTORIA, HASTA QUE ESTA FINALIZA
    public void animacionCaida() {

        if(revisarEstado()){ //revismaos el estadod e los jugadores
            return;
        }
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!disparo_en_curso) { //marcamos el disparo como en curso
                    disparo_en_curso = true;
                }
                terrain.borrarHitboxAnterior(); //eliminamos las hitbos anteriores de los tanques (se le creara a todos una nueva)
                boolean todosEnSuelo = true; //marcador para saber si estan todos los tanques en el suelo
                //SE VERIFICA EN QUE TERRENO SE ESTA REALIZANDO EL DISPARO
                if (terreno_random == 0) {
                    terrain.terreno_nieve(interfaz.gc, 0.0, 100, 1, terrain, alto, ancho);
                } else if (terreno_random == 1) {
                    terrain.terreno_desierto(interfaz.gc, 0.0, 100, 1, terrain, alto, ancho);
                } else if (terreno_random == 2) {
                    terrain.terreno_aram(interfaz.gc, 0.0, 100, 1, terrain, alto, ancho);
                }
                for (Jugador jugador : listJugador.getLista()) { //iteramos sobre jugador en la lista
                    if(!jugador.estaEliminado()) //si el jugador no esta marcado como eliminado procedemos a dibujarlo mientras cae
                    {
                        Tank tanque = jugador.getTanque();
                        int posicionInicialY = tanque.getPosicionY(); //posicion de donde cae

                        if (!tanque.estaSobreDuna(terrain)) { // si el tanque esta sobre dunas marcamos todos en el suelo como false ademas de ir sumando al eje y del tanque para que caiga
                            tanque.posicionY += Math.abs(Globales.gravedad);
                            todosEnSuelo = false;
                        }
                        //revisamos si esta en fuera de terreno
                        if(!tanque.esta_dentro_de_terreno(terrain))
                        {
                            // en caso de estar fuera del terrno paramos el animation y matamos al tanque
                            stop();
                            disparo_en_curso=false;
                            HBox muerte=VentanaEmergente.aparecer("Jugador "+jugador.nombre+" muerto...", 3);
                            interfaz.canvasPane.getChildren().add(muerte);
                            muerte.setLayoutX(Globales.alto_resolucion-300);
                            muerte.setLayoutY(0);
                            listJugador.getJugadorActual().suicidios++;// le agregamos un suicidio
                            listJugador.eliminarJugador(listJugador.getJugadorActual().jugador); //marcamos al jugador comoe liminado
                            Boolean terminar=revisarJugadores(); // revisamos el estado de las balas de los jugadores
                            if(terminar){ // si ninguno tiene balas terinamos el juego y retornamos de la funcion
                                return;
                            }
                            listJugador.generarTurnoAleatorio(); // generamos elnuevo turno del jugador y lo mostramos
                            interfaz.mostrarJugador(listJugador.getJugadorActual());

                            if (listJugador.getJugadorActual().tipo.equals("bot")) { // preguntamos si es un bot
                                iniciar_bot();
                            }
                            // 0 vivos = fin de ronda sin ganador (decisión mínima, revisar con el dueño)
                            if(listJugador.quedaUnoVivo()) // si queda uno vivo terminamos la ronda
                            {
                                finalizarRonda();
                            }
                            animacionCaida();// para que se redibujen los tanques
                            return;
                        }
                        if(contador_inicio!=0)
                        {
                            // revisamos de donde esta cayendo
                            int alturaCaida = tanque.getPosicionY() - posicionInicialY; // Calcular la altura de la caída
                            if (alturaCaida > tanque.dañoAltura) { // daño altura es de 10, tiene que caer de 10 de altura para hacer el daño
                                int dañoPorCaida = calcularDañoPorAltura(alturaCaida);                               
                                jugador.ajustar_vida(jugador.getVida(), dañoPorCaida);
                                if(jugador.vida<=0) // si el daño de la caida mato al tanque
                                {
                                    stop();
                                    disparo_en_curso=false;
                                    listJugador.getJugadorActual().suicidios++;// le agregamos un suicidio
                                    HBox muerte=VentanaEmergente.aparecer("Jugador "+jugador.nombre+" se a suicidado..."+"contador de suicidios->"+jugador.suicidios, 3);
                                    interfaz.canvasPane.getChildren().add(muerte);
                                    muerte.setLayoutX(Globales.alto_resolucion-300);
                                    muerte.setLayoutY(0);
                                    listJugador.eliminarJugador(listJugador.getJugadorActual().jugador);// marcamos al tanque como eliminado
                                    Boolean terminar=revisarJugadores(); // revisamos si los jugadores tienen balas
                                    if(terminar){
                                        return;
                                    }
                                    listJugador.generarTurnoAleatorio(); // generamos el turno aleatorio para el nuevo jugador
                                    interfaz.mostrarJugador(listJugador.getJugadorActual());
                                    if (listJugador.getJugadorActual().tipo.equals("bot")) {
                                        iniciar_bot();
                                    }

                                    // 0 vivos = fin de ronda sin ganador (decisión mínima, revisar con el dueño)
                                    if(listJugador.quedaUnoVivo()) // si queda uno vivo terminamos la ronda
                                    {
                                        finalizarRonda();
                                    }
                                    animacionCaida();
                                    return;
                                }
                            }
                        }
                        tanque.dibuarTanque(interfaz.gc); // si paso por todo el tanque lo dibujamos
                        tanque.modificarCañon(interfaz.gc, 0);// modificamos su cañon para que este en la posicion donde este cayendo

                        if (todosEnSuelo) { // una vez que todos los tanques esten en el suelo les creamos sus hitbox
                            tanque.crearHitbox(interfaz.gc, terrain,jugador);

                        }
                    }
                }
                if (todosEnSuelo) {// ya que se marco anteriormente sis hitbox y se colocaron todos en el suelo
                    disparo_en_curso=false;
                    stop();
                    Boolean terminar=revisarJugadores();// marcamos a los jugadores que no podran jugar
                    if(terminar) {
                        return;
                    }
                    listJugador.generarTurnoAleatorio();//cambiamo turno
                    contador_inicio++;
                    interfaz.mostrarJugador(listJugador.getJugadorActual());
                    if (listJugador.getJugadorActual().tipo.equals("bot")) {
                        iniciar_bot();
                    }
                }
            }
        }.start();
    }


    public void elegir_bala(){//METODO QUE CONTIENE LOS BOTONES PARA ESCOGER UNA BALA Y SUS ACCIONES
        interfaz.disparar.setDisable(true);//no podemos disparar mientras escogemos la bala
        
        interfaz.bala1.setOnAction(event -> {//escoge bala 1
            Musica.sonido_click();
            String int_string = Integer.toString(listJugador.getJugadorActual().getCantidad60());
            interfaz.textcantidad1.setText(int_string);//muestra la cantidad de balas disponibles       
            tipo=1;//ajusta el tipo          
            interfaz.disparar.setDisable(false);
        });
            
        interfaz.bala2.setOnAction(event -> {//escoge bala 2 
            Musica.sonido_click();
            String int_string = Integer.toString(listJugador.getJugadorActual().getCantidad80());
            interfaz.textcantidad2.setText(int_string);//lo mismo de bala1
            tipo=2;//ajusta el tipo   
            interfaz.disparar.setDisable(false);
        });
            
        interfaz.bala3.setOnAction(event -> {//escoge bala 3     
            Musica.sonido_click();
            String int_string = Integer.toString(listJugador.getJugadorActual().getCantidad105());
            interfaz.textcantidad3.setText(int_string);
            tipo=3;//ajusta el tipo       
            interfaz.disparar.setDisable(false);
        });                      
    }
    
    public void reiniciar_partida(){//PROCESO PARA REINICIAR TODO UN PARTIDO DESDE CERO
        jugando=false;//definimos el estado como falso
        listJugador.revivir_jugadores();//revivimos y reiniciamos todo de los jugadores
        escenaTienda.inicializarInterfaz(listJugador);//abrimos tienda
                
    }
    public static int getRandom(){
        return terreno_random;
    }

    void definirPosicion() {//define la posicion en la que inician y caen los tanques
        if (revisarEstado()) {
            return;
        }
        if(Globales.jugadores_def==2){
            int largo = Globales.alto_resolucion-200;
            //jugador 1
            int min=50;
            int max=largo/2+50;
            int posicion_inicial=random.nextInt(max-min)+min;
            listJugador.getLista().get(0).posicionInicalX=posicion_inicial;
            //jugador 2
            min=max;
            max=largo-50;
            posicion_inicial=random.nextInt(max-min)+min;
            listJugador.getLista().get(1).posicionInicalX=posicion_inicial;
            
        }
        
        else{
            int largo=Globales.alto_resolucion - 200;
            int ancho_segmento = largo / Globales.jugadores_def;

            int aux = -1;  //inicializar con un valor que no se encuentre en el rango

            for (int i = 0; i < Globales.jugadores_def; i++) {
                int min = ancho_segmento * i;
                int max = ancho_segmento * (i + 1);
                System.out.println(min+"-"+max);
                //ajustamos el maximo y el minimo
                min=min+10;

                //definir la posición inicial
                double factorDispersión = 1.5;
                int nuevaPosicion = generarPosicionUnica(min, max, aux, factorDispersión);
                if(nuevaPosicion>largo)
                {
                    nuevaPosicion-=200;
                }
                listJugador.getLista().get(i).posicionInicalX = nuevaPosicion;
                System.out.println("posicion geenerada -> x" + nuevaPosicion);

                //actualizar aux con la nueva posición
                aux = nuevaPosicion;
            }
        }
       
    }
    
    //genera posiciones que no varian para evitar casos en los que se juntan los tanques
    private int generarPosicionUnica(int min, int max, int posicionAnterior, double factorDispersión) {
        Random random = new Random();
        int distanciaMinima = 75;
        int nuevaPosicion;
        do {
            nuevaPosicion = random.nextInt((int) (factorDispersión * (max - min))) + min;
        } while (Math.abs(nuevaPosicion - posicionAnterior) < distanciaMinima);

        return nuevaPosicion;
    }

    public int calcularDañoPorAltura(int altura) {//calcula el damage por altura
        return (int) (altura * Math.abs(Globales.gravedad)/ 100);
    }
       
    // 0 vivos = fin de ronda sin ganador (decisión mínima, revisar con el dueño)
    public static Jugador cerrarRondaLogica(ListaJugadores listJugador) {
        if (listJugador == null) {
            return null;
        }
        // Determinar ganador antes de revivir a los jugadores.
        Jugador ganador = listJugador.getGanadorRonda();
        // Con 0 vivos no debe asignarse victoria a nadie ni lanzar excepción.
        Jugador.pagar_ronda(listJugador);
        listJugador.revivir();
        return ganador;
    }

    public void finalizarRonda(){//se finaliza una ronda una vez que se cumplan las condiciones       
        cerrarRondaLogica(listJugador);
        terrain.borrarHitboxAnterior();//eliminamos las hitbox anteriores
        jugando=false;//ya no se esta jugando
        resultados.mostrarTabla(escenaTienda,listJugador);//muestra la clasificacion
             
    } 
       
    public void generarTerrenoNuevo(){//Genera un terreno diferente al anterior
        int terrenoAnterior=terreno_random;
        do{
            terreno_random=random.nextInt(3);
        }while (terreno_random == terrenoAnterior);
    }
    
    public void finalizarJuego(){//se cierra la ejecucion
        Platform.exit();
    }
    
    public void elegir_bala_bot(){
        tipo=random.nextInt(3)+1;
    }

    public void animacionBala(Bala nuevaBala) {
        if(revisarEstado()){ //revisamos el estado de los jugadores
            return;
        }
        new AnimationTimer() { //iniciamos el animation timer

            long lastWindChangeTime = 0; //variable para almacenar la última vez que cambió la dirección del viento
            int windChangeInterval = 50_000_000; //cambiar la dirección del viento cada 1/20 de segundo
            double vientoIncremento = viento/ 20.0; //reducir la cantidad que se suma al eje x a 1/20

            @Override
            public void handle(long now) {
                if (now - Globales.lastFrameTime >= Globales.timePerFrame) {
                    Globales.lastFrameTime = now;

                    if (!disparo_en_curso) { //marcamos el disparo como en curso
                        disparo_en_curso = true;
                    }

                    nuevaBala.dibujo(interfaz.gc, nuevaBala.getDanio()); //dibujamos la bala
                    //actualizar la posición de la bala
                    nuevaBala.actualizarPosicion(deltaTiempo, nuevaBala, distancia, altura, interfaz.boxdistancia, interfaz.boxaltura, listJugador.getJugadorActual().getTanque().getCañonY(), listJugador.getJugadorActual().getTanque().getCañonX());

                    //cambiar la dirección del viento cada 1/20 de segundo
                    if (now - lastWindChangeTime >= windChangeInterval) {
                        lastWindChangeTime = now;
                        nuevaBala.ejeX = nuevaBala.ejeX + vientoIncremento; //sumar 1/20 al eje x para simular el viento
                    }
                    //se guarda el valor en donde se colisiono la bala                    
                    impacto = terrain.colision_terreno(interfaz.gc, nuevaBala, terrain.dunas, terrain.matriz, tipo);

                    if (impacto != 0) { //si impacto es distinto de 0 a impactado a un jugador la bala
                        impacto_jugador(impacto, nuevaBala.getDanio()); //le aplciamos el daño correspondiente
                    }

                    if (nuevaBala.eliminar()) { // si la bala choco con el terreno eliminamos la bala
                        
                        stop(); //paramos el animation
                        colision_bala(); //revisamos daño de la bala si es que a impactado a algun jugador
                        Globales.congelar(1);
                        disparo_en_curso = false;
                        viento=Bala.cambiarViento(interfaz);//cambiamos la dirección del viento
                        interfaz.estadisticas(listJugador);
                        animacionCaida();
                    }
                }
            }
        }.start();
    }

    public void iniciar_bot() {
        if(revisarEstado()){ //revisamos el estado de los jugadores
            return;
        }
        if (!disparo_en_curso) { //si animacion bala termino

            List<Integer> tiposDisponibles = new ArrayList<>();//creamos una lista de tipos de balas disponibles y la llenamos con 1 2 3
            tiposDisponibles.add(1);tiposDisponibles.add(2);tiposDisponibles.add(3);

            boolean balaEncontrada = false; //inicializamos un marcador

            //verifica cada tipo de bala hasta encontrar una con municiones o agotar todas las opciones
            while (!tiposDisponibles.isEmpty() && !balaEncontrada) {
                
                int indiceAleatorio = random.nextInt(tiposDisponibles.size());
                tipo = tiposDisponibles.get(indiceAleatorio);
                balaEncontrada = !Jugador.comprobarMunicion(tipo,listJugador);

                if (!balaEncontrada) {
                    //si no hay municiones para este tipo de balas, se elimina de la lista y se prueba el siguiente
                    tiposDisponibles.remove(indiceAleatorio);
                    tipo=0;
                } //no hace falta una condicion si el jugador no tiene balas ya que se hizo la verificacion anteriormente
            }
            revisarJugadores();

            //configuración del disparo de la bala para el bot
            velocidad = random.nextDouble() * 35 + 30; 
            angulo = 60 + random.nextDouble() * 60; //angulos entre 60 y 120
            angulo=(-1)*angulo;//ajuste pq estan invertidos los angulos

            if(tipo==1){
                interfaz.textcantidad1.setText(Integer.toString(listJugador.getJugadorActual().getCantidad60()));
            }
            if(tipo==2){
                interfaz.textcantidad2.setText(Integer.toString(listJugador.getJugadorActual().getCantidad80()));
            }
            if(tipo==3){
                interfaz.textcantidad3.setText(Integer.toString(listJugador.getJugadorActual().getCantidad105()));
            }
            //Indica que esta jugando un bot actualmente
            HBox textBot=VentanaEmergente.aparecer("Jugando un bot...",2);
            interfaz.canvasPane.getChildren().add(textBot);
            textBot.setLayoutX(Globales.alto_resolucion/2-70);
            textBot.setLayoutY(0);
            //crear y disparar la bala
            Bala nuevaBala = crear_bala();
            Musica.sonido_disparo();
            animacionBala(nuevaBala);
        }
    }
  
    public Boolean revisarJugadores(){//revisa el estado general de los jugadores para ver si se debe seguir jugando
        for(Jugador jugador : listJugador.lista){
            if((jugador.cantidad105+jugador.cantidad80+jugador.cantidad60)<=0||jugador.eliminado==true){
                System.out.println(" se desactivo ->"+(jugador.jugador+1));
                listJugador.desactivarJugador(jugador.jugador);
            }
        }
        if(listJugador.quedanActivos())
        {   
            finalizarRonda();
            return true;
        }
        return false;
    }
  
    public boolean revisarEstado(){//metodo que controla si todas las funciones deberian seguir ejecutandose(osea que el juego sigue o no)
        return jugando == false;
    }

}
