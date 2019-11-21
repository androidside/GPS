#  Serial Parser to read GPS messages using the National Marine Electronics Association protocol
This software is developed for the BETTII project by Jordi Vila at NASA Goddard Space Flight Center.

![Thermometry](bettii.png)

1. The code uses Google's protobufs in order to send data to Aurora, BETTII's ground control software


>**NOTE:** The code is reused from the Thermometry Dewar project, used to read temperatures from star detectors. TextFiles folder contains raw data dumped from the dewar boards that allows to test the program when not conected to the dewar and star detectors. The fReadingFile boolean has to be set to true on ThermometryReaderMain.java
