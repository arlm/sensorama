# Sensorama

Sensorama is a data science experiment. Its purpose is to see if information
gathered from modern sensors could be turned into understanding of real
events from the sensor's place of origin.  For example, for the mobile cell
phone which comes with accelerometer and a gyroscope, when enough sensor
data is collected and analyzed, it could be interesting to see if Sensorama
could detect activities such as jogging or playing drums. Interesting
experiment could consist of a smartphone attach to a drummers arm and
Sensorama being able to understand patterns played by the musician.

Initial implementation of Sensorama is meant to be an exercise in mobile
cross-platform mobile programming in Android and iOS ecosystems.

# Introduction

Sensors come in a wide variety of types, but the most popular are
accelerometer, temperature, gravity, gyroscope and proximity sensors. All
these sensors act as a smart A/D converters, in which MEMS chip translates
analog singals from the environment to something modern processors can
understand. Software layer provides a way to enable the sensor, and later
translates these events into numeric data which is provided through a sensor
data source. Programs typically subscribe to these sources to get notified
about the state change of the sensor.

# Design

Design of Sensorama consists of frontend UI facing end user, thin controller
interacting with main Sensorama Core class and model, which in turn
interacts with various sensors. Even though in this and further examples
cell phone programming platforms are mentioned, design ought to be
prepared to encompass embedded dongles with handful of various sensors,
which send a sensor data through the wireless channel. SensorTag from TI is
an example of such a dongle.

# Data encoding

The Sensorama core must encompass slight differences in different cell phone
designs and the number and type of different sensors. For example, some
phones may have a humidity sensor, while others may not. Design should
encompass this so that without application redesign, more feature rich
phones can submit more data without causing a data from less featured phones
to be invalidated. Type of a sensor should be thus included as a tag along
with provided data, or a handshake protocol should be included as a way of
discovering of what the format of transmitted data will be.

# Data

Sensors vary greatly in ways in which they submit data. For example, the
battery sensor may only send an update every couple of seconds, while the
accelerometer may do it continually. While makes sense to capture the data
from an accelerometer on a regular basis with short intervals, capturing the
data from a battery every couple of milliseconds makes little sense. Thus it
might be performed so that the data samples aren't uniform, meaning that the
sample format can vary in between submissions.

# Timestamping

Data from sensors should be tagged with time too. Tagging with time is
necessary, since it is important to understand the time elapsed in between
samples. It too may provide an insight into understanding the cell phone
feasibility as a sensoring platforms, since thread and process scheduling on
a phone may turn out to be inaccurate for very precise sample timestamping.
For example: timers which are meant to fire and execute a function every
250ms may in reality take longer or may be queued together. In case this
hipothesis proves incorrect, it should be possible to adjust amount of data
submitted from the phone. One may imagine two types of sample timestamping:
absolute and relative. Absolute timestamp would return a
microsecond-accurate wall-clock time in situation where accuracy of sampling
is unclear. Relative timestamping coule be used when a sampling accuracy is
known, in which case the wallclock time could be submitted only once. Every
further sample could be either tagged with a 32-bit number, or maybe not
tagged at all (implying that data arrives in order or that order doesn't
matter).
