
/* Variable para recoger la lectura de bateria*/
float sensorPin = A0;    
/* Variable para recoger la lectura de corriente*/
float corrientePin = A3;
float valueCorriente= 0.0;
int sensorValue = 0;  
int bateria =0;
float corriente = 0.0;
/* Valor real de la resistencia de 22ohm para medir amperaje*/
float R = 1.3;

void setup() {
  Serial.begin(9600);
  delay(100);
}

void loop() {
  
  // read the value from the sensor:
  sensorValue = analogRead(sensorPin);
  valueCorriente = analogRead(corrientePin);
  bateria=map(sensorValue,0,716,0,100);
 
  delay(100);
  Serial.println("AT$RC");
  delay(10);
  Serial.print("AT$SF=");
  if(bateria<16) Serial.print('0');
  Serial.println(bateria,HEX);
  delay(20000);
  Serial.println(bateria);
  
  /*corriente = (( valueCorriente * (3.5/ 1023.0) )/ R) * 1000;
  if(corriente<10) Serial.print("STAND BY: ");
  if(corriente>10) Serial.print("Transmisión: ");
  Serial.println(corriente);
 // Serial.println();
  delay(200);*/
  
}
