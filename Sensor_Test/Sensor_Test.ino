#include <Wire.h>
#define HTDU21D_ADDRESS 0x40  //Unshifted 7-bit I2C address for the sensor
#define TRIGGER_TEMP_MEASURE_HOLD  0xE3
#define TRIGGER_HUMD_MEASURE_HOLD  0xE5
#define TRIGGER_TEMP_MEASURE_NOHOLD  0xF3
#define TRIGGER_HUMD_MEASURE_NOHOLD  0xF5
#define WRITE_USER_REG  0xE6
#define READ_USER_REG  0xE7
#define SOFT_RESET  0xFE

byte sensorStatus;
int UVOUT = A0; //Output from the sensor
int refLevel = 590; //3.3V power on the Arduino board CHOCA CON CO!!!!!
float aqi_CO;
float aqi_NO2;
float aqi_O3;
float aux = 0;
float AQI;
int AQI_x=0;
int UV=0;
int CO_svx = 0;
int NO2_svx = 0;
int Oz_svx = 0;
float CO_sv = 0;
float NO2_sv = 0;
float Oz_sv = 0;
int temp2=0;
int hum=0;
void setup() {
  Serial.begin(9600);
  pinMode(UVOUT, INPUT);
  
  Wire.begin();
}

void loop() {
  aux = 0;

  CO_sv = analogRead(A1); // checar entrada
  NO2_sv = analogRead(A2);//checar entrada
  Oz_sv = analogRead(A3);// checar entrada
  unsigned int rawHumidity = htdu21d_readHumidity();
  unsigned int rawTemperature = htdu21d_readTemp();

  float temperature = calc_temp(rawTemperature);
  float relativeHumidity = calc_humidity(rawHumidity); //Turn the humidity signal into actual humidity
  int uvLevel = averageAnalogRead(UVOUT);
  //int refLevel = averageAnalogRead(REF_3V3);

  //Use the 3.3V power pin as a reference to get a very accurate output value from sensor
  float outputVoltage = 3.3 / refLevel * uvLevel;

  float uvIntensity = mapfloat(outputVoltage, 0.99, 2.9, 0.0, 15.0);
  aux = Oz_sv;
  Oz_sv = sqrt(pow((aux - 641.59), 2)) / 1000;
  //-----------------------------------------------------------------
  if (Oz_sv >= 0.000 && Oz_sv <= 0.059)
    aqi_O3 = (50 / .059) * (Oz_sv);
  if (Oz_sv >= 0.060 && Oz_sv <= 0.075)
    aqi_O3 = (100 - 51 / 0.075 - 0.060) * (Oz_sv - 0.60) + 51 ;
  if (Oz_sv >= 0.076 && Oz_sv <= 0.095)
    aqi_O3 = (150 - 101 / 0.095 - 0.076) * (Oz_sv - 0.076) + 101;
  if (Oz_sv >= 0.096 && Oz_sv <= 0.115)
    aqi_O3 = (200 - 151 / 0.115 - 0.096) * (Oz_sv - 0.096) + 151;
  if (Oz_sv >= 0.116 && Oz_sv <= 0.374)
    aqi_O3 = (300 - 201 / 0.374 - 0.116) * (Oz_sv - 0.116) + 201;
  //-----------------------------------------------------------------
  if (NO2_sv >= 0.000 && NO2_sv <= 53)
    aqi_NO2 = (50 / 53) * (NO2_sv);
  if (NO2_sv >= 54 && NO2_sv <= 100)
    aqi_NO2 = (100 - 51 / 100 - 54) * (NO2_sv - 54) + 51 ;
  if (NO2_sv >= 101 && NO2_sv <= 360)
    aqi_NO2 = (150 - 101 / 360 - 101) * (NO2_sv - 101) + 101;
  if (NO2_sv >= 361 && NO2_sv <= 649)
    aqi_NO2 = (200 - 151 / 649 - 361) * (NO2_sv - 361) + 151;
  if (NO2_sv >= 650 && NO2_sv <= 1249)
    aqi_NO2 = (300 - 201 / 1249 - 649) * (NO2_sv - 650) + 201;
  //----------------------------------------------------------------
  if (CO_sv >= 0.000 && CO_sv <= 4.4)
    aqi_CO = (50 / 4.4) * (CO_sv);
  if (CO_sv >= 4.5 && CO_sv <= 9.4)
    aqi_CO = (100 - 51 / 9.4 - 4.5) * (CO_sv - 4.5) + 51 ;
  if (CO_sv >= 9.5 && CO_sv <= 12.4)
    aqi_CO = (150 - 101 / 12.4 - 9.5) * (CO_sv - 9.5) + 101;
  if (CO_sv >= 12.5 && CO_sv <= 15.4)
    aqi_CO = (200 - 151 / 15.4 - 12.5) * (CO_sv - 12.5) + 151;
  if (CO_sv >= 15.5 && CO_sv <= 30.4)
    aqi_CO = (300 - 201 / 130.4 - 15.5) * (CO_sv - 15.5) + 201;
  //---------------------------------------------------------------

  if (aqi_O3 > aqi_CO && aqi_O3 > aqi_NO2)
    AQI = aqi_O3;
  if (aqi_CO > aqi_O3 && aqi_CO > aqi_NO2)
    AQI = aqi_CO;
  if (aqi_NO2 > aqi_CO && aqi_NO2 > aqi_O3)
    AQI = aqi_NO2;
  //----------------------------------------------------------------
    temp2=(((temperature)*1.8)+32);
    UV=uvIntensity;
    CO_svx=CO_sv;
    NO2_svx=NO2_sv;
    Oz_svx=analogRead(A3); //  este valor fue el que probamos ya que, como recuerdan se configuro mal el sensor de ozono y en la ultima prueba tome el valor directo de la entrada analogica A3
    
    AQI_x=AQI;
    hum=relativeHumidity;
  Serial.print(UV);
  Serial.print(",");
  Serial.print(CO_svx);
  Serial.print(",");
  Serial.print(NO2_svx);
  Serial.print(",");
  Serial.print(Oz_svx); 
  Serial.print(",");
  Serial.print(AQI_x);
  Serial.print(",");
  Serial.print(hum);
  Serial.print(",");
  Serial.println(temp2);
delay(5000);
}

unsigned int htdu21d_readTemp()
{
  //Request the temperature
  Wire.beginTransmission(HTDU21D_ADDRESS);
  Wire.write(TRIGGER_TEMP_MEASURE_NOHOLD);
  Wire.endTransmission();

  //Wait for sensor to complete measurement
  delay(60); //44-50 ms max - we could also poll the sensor

  //Comes back in three bytes, data(MSB) / data(LSB) / CRC
  Wire.requestFrom(HTDU21D_ADDRESS, 3);

  //Wait for data to become available
  int counter = 0;
  while (Wire.available() < 3)
  {
    counter++;
    delay(1);
    if (counter > 100) return 998; //Error out
  }

  unsigned char msb, lsb, crc;
  msb = Wire.read();
  lsb = Wire.read();
  crc = Wire.read(); //We don't do anything with CRC for now

  unsigned int temperature = ((unsigned int)msb << 8) | lsb;
  temperature &= 0xFFFC; //Zero out the status bits but keep them in place

  return temperature;
}

//Read the humidity
unsigned int htdu21d_readHumidity()
{
  byte msb, lsb, checksum;

  //Request a humidity reading
  Wire.beginTransmission(HTDU21D_ADDRESS);
  Wire.write(TRIGGER_HUMD_MEASURE_NOHOLD); //Measure humidity with no bus holding
  Wire.endTransmission();

  //Hang out while measurement is taken. 50mS max, page 4 of datasheet.
  delay(55);

  //Read result
  Wire.requestFrom(HTDU21D_ADDRESS, 3);

  //Wait for data to become available
  int counter = 0;
  while (Wire.available() < 3)
  {
    counter++;
    delay(1);
    if (counter > 100) return 0; //Error out
  }

  msb = Wire.read();
  lsb = Wire.read();
  checksum = Wire.read();

  unsigned int rawHumidity = ((unsigned int) msb << 8) | (unsigned int) lsb;
  rawHumidity &= 0xFFFC; //Zero out the status bits but keep them in place

  return (rawHumidity);
}

//Given the raw temperature data, calculate the actual temperature
float calc_temp(int SigTemp)
{
  float tempSigTemp = SigTemp / (float)65536; //2^16 = 65536
  float realTemperature = -46.85 + (175.72 * tempSigTemp); //From page 14

  return (realTemperature);
}

//Given the raw humidity data, calculate the actual relative humidity
float calc_humidity(int SigRH)
{
  float tempSigRH = SigRH / (float)65536; //2^16 = 65536
  float rh = -6 + (125 * tempSigRH); //From page 14

  return (rh);
}

//Read the user register
byte read_user_register(void)
{
  byte userRegister;

  //Request the user register
  Wire.beginTransmission(HTDU21D_ADDRESS);
  Wire.write(READ_USER_REG); //Read the user register
  Wire.endTransmission();

  //Read result
  Wire.requestFrom(HTDU21D_ADDRESS, 1);

  userRegister = Wire.read();

  return (userRegister);
}

//Write to the user register
//NOTE: We disable all bits except for measurement resolution
//Bit 7 & 0 = Measurement resolution
//Bit 6 = Status of battery
//Bit 5/4/3 = Reserved
//Bit 2 = Enable on-board heater
//Bit 1 = Disable OTP reload
void write_user_register(byte thing_to_write)
{
  byte userRegister = read_user_register(); //Go get the current register state
  userRegister &= 0b01111110; //Turn off the resolution bits
  thing_to_write &= 0b10000001; //Turn off all other bits but resolution bits
  userRegister |= thing_to_write; //Mask in the requested resolution bits

  //Request a write to user register
  Wire.beginTransmission(HTDU21D_ADDRESS);
  Wire.write(WRITE_USER_REG); //Write to the user register
  Wire.write(userRegister); //Write to the data
  Wire.endTransmission();
}

#define SHIFTED_DIVISOR 0x988000 //This is the 0x0131 polynomial shifted to farthest left of three bytes

unsigned int check_crc(uint16_t message_from_sensor, uint8_t check_value_from_sensor)
{
  
  uint32_t remainder = (uint32_t)message_from_sensor << 8; //Pad with 8 bits because we have to add in the result/check value
  remainder |= check_value_from_sensor; //Add on the check value

  uint32_t divsor = (uint32_t)SHIFTED_DIVISOR;

  for (int i = 0 ; i < 16 ; i++) //Operate on only 16 positions of max 24. The remaining 8 are our remainder and should be zero when we're done.
  {
    
    if ( remainder & (uint32_t)1 << (23 - i) ) //Check if there is a one in the left position
      remainder ^= divsor;

    divsor >>= 1; //Rotate the divsor max 16 times so that we have 8 bits left of a remainder
  }

  return remainder;
}
int averageAnalogRead(int pinToRead)
{
  byte numberOfReadings = 8;
  unsigned int runningValue = 0;

  for (int x = 0 ; x < numberOfReadings ; x++)
    runningValue += analogRead(pinToRead);
  runningValue /= numberOfReadings;

  return (runningValue);
}

float mapfloat(float x, float in_min, float in_max, float out_min, float out_max)
{
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}
