Fabflix is a web-based movie database application that comes with an associated Android application, enabling users to explore, search, and purchase movies. Throughout this project, I constructed the complete architecture of both the website and Android app from scratch, ensuring seamless integration with a shared backend API. The hosting of Fabflix is carried out on an AWS EC2 instance, while Apache Tomcat is employed to serve both the website and API. The movie database, powered by MySQL, is where the movie-related data is stored.

In order to bolster security measures, a range of precautions were implemented, including the adoption of HTTPS, integration of reCAPTCHA, utilization of PreparedStatement queries, implementation of sessions, and incorporation of a login filter. These technological safeguards collectively safeguarded user information against potential malicious attacks.

Furthermore, an ETL (Extract, Transform, Load) pipeline was established to process large XML files and insert large amounts of data into the movie database. This new data included more detailed movie information, ultimately enhancing the precision of search results and movie recommendations.

Technologies Used: Java, MySQL, HTML, CSS, Javascript, Apache Tomcat, Apache Maven, ETL pipeline, AWS
