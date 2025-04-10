FROM public.ecr.aws/amazoncorretto/amazoncorretto:21.0.6-al2023-headful

RUN yum update && yum install -y  net-tools telnet

WORKDIR /app

ENV MONGODB_URI <your mongodb connection string>

COPY target/monitoring*.jar monitoring.jar

CMD ["java","-jar","/app/monitoring.jar"]