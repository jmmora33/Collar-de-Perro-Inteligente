#include <DHT.h>
#include <SoftwareSerial.h>
#include <Servo.h>

//CONFIG BT
#define RECUBT 2000
 
 const int puerta = 2;
 const int temp1 = 4;
 const int temp2 = 5;
 const int temp3 = 6;
 const int luz = 8;
 const int collar = 10;
 const int ladrido = 12;
 const int aTemp = 14;
 
//Pines temperatura
#define dhtPin 6
#define rojoPin 4
#define azulPin 5

// PIN RELAY
#define relay  9
//Pin Mic
#define MICPIN A1
#define UMBRALMIC 500
#define CANTLADRIDO 10
#define RECUMIC 500
//Pines alarma collar desprendido
#define ledPin 3
#define buzzPin 2
#define prPin A0

//Config temperatura
#define DHTTYPE DHT11//Seleciona el tipo de sensor
DHT dht(dhtPin, DHTTYPE);//Configura la libraría
#define RECUTEMP 2000
#define RECUSERV 2000

//Ultrasonido
#define ULTRASONIDO 7

//Servo
#define SERVO 11
Servo servo; //declaración servo

//Bluetooth

//temperatura
#define EXT_CALOR 21 //Extremo para encender alarma calor
#define EXT_FRIO 20 //Extremo para encender alarma frio

//Config AlarmaCollar
float valorSens = 0;
#define UMBRAL 800.00 //Valor de sensibilidad para collar desprendido


//VARIABLES GLOBALES
int contLadrido = 0;
int temp = 0;
float tiempoUlt = 0;
float tiempoTemp = 0;
float tiempoBt = 0;
float tiempoServ = 0;
float tiempoBuzz = 0;
float tiempoMic = 0;
float tiempoAlarm = 0;
int valorPuerta = 0;
int flag = 0;
char inputStream;
char outputStream[50];
String output;
void setup() {
 configurar_temp();
 configurar_alarmaCollar();
 configurar_ultrasonido();
 configurar_servo();
 configurar_relay();
 configurar_bluetooth();
 configurar_mic();
}

void loop() {
  recibirMensaje();
  outputStream[ladrido] = '0';
  
  
  if(analogRead(MICPIN) > UMBRALMIC)
  {
    contLadrido++;
    if(contLadrido > CANTLADRIDO)
    {
      //Perro ladro mucho
      ejecutar_ultrasonido();
      contLadrido = 0;
      outputStream[ladrido] = '1';
    }
  }
  
  if(millis() - tiempoTemp > RECUTEMP)
  {
    ejecutar_temp();
    tiempoTemp = millis();
  }
  ejecutar_alarmaCollar();
  if(millis() - tiempoServ > RECUSERV)
  {
    ejecutar_servo(inputStream);
    tiempoServ = millis();
  }
  
 // outputStream[10] = '0'; //apaga flag ladrido
  if(millis() - tiempoBt > RECUBT)
  {
    //BT.write("#puertaAbierta,22&");
    tiempoBt = millis();
    Serial1.println(outputStream);
    Serial.println(outputStream);  
  }

  //Alarma
  if(flag == 1)
  {
      if((millis()-tiempoBuzz > 100))
      {
        digitalWrite(ledPin, LOW);
        analogWrite(buzzPin, 0);
        tiempoBuzz = millis();
      }
        digitalWrite(ledPin, HIGH);
        analogWrite(buzzPin, 100);
    if((millis()-tiempoAlarm > 10000))
    { 
      flag = 0;
      tiempoAlarm = millis();  
    }
  }
}

void configurar_bluetooth()
{
 
 Serial.begin(9600);
 Serial1.begin(9600);

 outputStream[0] = '#';
 outputStream[1] = ',';
 outputStream[3] = ',';
 outputStream[7] = ',';
 outputStream[9] = ',';
 outputStream[11] = ',';
 outputStream[13] = ',';
 outputStream[15] = ',';
 outputStream[16] = '&'; 
}
void configurar_mic(){
  pinMode(MICPIN, INPUT);
}

void configurar_temp(){
  pinMode(dhtPin, INPUT);
  pinMode(rojoPin, OUTPUT);
  pinMode(azulPin, OUTPUT);
  tiempoTemp = millis();
  dht.begin();
  
}

void ejecutar_temp(){
    temp = dht.readTemperature();
    char tempchar[4];
    String ra;
    int i = temp1;
    ra = String(temp);
    //normalizo la temperatura para que siempre sea de tres digitos (dos de valor y uno de signo)
    if(temp >= 0)
    {
      ra = "+" + ra;
    }
    while(ra.length() < 3)
    {
      ra = "0" + ra;
    }
    
    ra.toCharArray(tempchar, 4);
    outputStream[aTemp] = '0'; //0 es neutro
    
    //GUardo la temperatura leida en la string para enviar
    for(i = temp1; i <= temp3 ; i++)
    {
      outputStream[i] = tempchar[i-temp1];
    }
    
    if(temp >= EXT_CALOR)
    {
      digitalWrite(rojoPin, HIGH);
      digitalWrite(azulPin, LOW);
      //NOTIF CALOR EXTREMO
      outputStream[aTemp] = '2';
    }
    
    if(temp <= EXT_FRIO)
    {
      digitalWrite(rojoPin, LOW);
      digitalWrite(azulPin, HIGH);
      //NOTIF FRIO EXTREMO
      outputStream[aTemp] = '1';
    }
    // String pepe #,variable1,variable2,...,variableN,&
   //BT.write(pepe); Ver si recibe string
  
  }

  void configurar_ultrasonido(){
  pinMode(ULTRASONIDO,OUTPUT);
  digitalWrite(ULTRASONIDO,LOW);
  //CONFIGURAMOS ACA LOS MICROFONOS  
  }

  void configurar_servo(){
    tiempoServ = millis();
    servo.attach(SERVO); //Configuración servo
    servo.write(150);
}

  void configurar_alarmaCollar(){
    tiempoBuzz = millis(); 
    pinMode(ledPin, OUTPUT);
    pinMode(buzzPin, OUTPUT);
    pinMode(prPin, INPUT);
    digitalWrite(ledPin, LOW);
    digitalWrite(buzzPin, LOW);
  }
  
  void ejecutar_ultrasonido()
  {
    if((millis()-tiempoUlt < 100))
    {
      digitalWrite(ULTRASONIDO,HIGH);
    }
    else
    {
      digitalWrite(ULTRASONIDO,LOW);
      tiempoUlt = millis();
    }
  }
  
  void ejecutar_alarmaCollar(){
    valorSens = analogRead(prPin);
    
    if(valorSens > UMBRAL)
    {
      if((millis()-tiempoBuzz > 100))
      {
        digitalWrite(ledPin, LOW);
        analogWrite(buzzPin, 0);
        tiempoBuzz = millis();
      }
        digitalWrite(ledPin, HIGH);
        analogWrite(buzzPin, 100);
        //Enviar notif collar desprendido
        outputStream[collar] ='1';
   }
    else
   {
    
      digitalWrite(ledPin, LOW);
      analogWrite(buzzPin, 0);
      //Notif collar ok
      outputStream[collar] = '0';
   }
   

 }
  
  
void ejecutar_servo(char valor){ //valor = 0 entonces apertura valor = 1 entonces cierre
    
    if(valor == '1')
    {
      servo.write(10); //abrir
 
    }
    else if (valor == '2')
    {
      servo.write (150); //cerrar
      
    }
    if(servo.read() == 10)
    {
         outputStream[puerta] = '1'; //1 = puerta abierta
    }
    else
    {
         outputStream[puerta] = '0'; 
    }
  }
void configurar_relay()
{
  
  pinMode(relay, OUTPUT);
  digitalWrite(relay, LOW);
  outputStream[luz] = '0';
}

void ejecutar_relay(char param){
  if(param == '3')
   {
      //encender relay
      digitalWrite(relay, HIGH);
      outputStream[luz] = '1';
   }
   if(param == '4')
   {
      //encender relay
      digitalWrite(relay, LOW);
      outputStream[luz] = '0';
   }
}

void recibirMensaje(){
if(Serial1.available())
    {
      inputStream = Serial1.read();
      tiempoBt = millis();
      Serial.println("Lectura mensaje celu");
      Serial.println(inputStream);
      switch (inputStream)
      {
        case '1': ejecutar_servo(inputStream); 
        break;
        case '2': ejecutar_servo(inputStream); 
        break;
        case '3': ejecutar_relay(inputStream);
        break;
        case '4': ejecutar_relay(inputStream);
        break;
        case '5': ejecutar_ultrasonido();
        break;
        case '6': flag = 1;
                  
        break;
      }
    }  
}

