# OpenTelemetry Java Observability Demo

## **📌 Objetivo**
Criar uma aplicação Java simples e demonstrar **diferentes formas de instrumentação e observabilidade**, passando por:
1. **Métricas JMX**: Inspecionar métricas da JVM usando `jcmd` e `JConsole`.
2. **OpenTelemetry Agent**: Usar um agente para coletar métricas automaticamente e exportá-las via OTLP.
3. **Micrometer**: Integrar a aplicação com Micrometer e comparar com OpenTelemetry.
4. **Outros agentes (Datadog, etc.)**: Explorar outras ferramentas de observabilidade gratuitas.

---

## **📌 Etapa 1: Criando uma Aplicação Java Simples (Sem Instrumentação)**

### **🔹 Pré-requisitos**
- **Maven** 3.9.x
- **Java** 17+

### **🔹 1. Criar o projeto Maven**
O projeto foi criado usando o archetype:
```sh
mvn archetype:generate -DgroupId=com.haikal.otel -DartifactId=otel-demo \
    -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

### **🔹 2. Estrutura do projeto**
```
otel-demo
│── src
│   ├── main
│   │   ├── java
│   │   │   └── com/haikal/otel/App.java
│   ├── test
│   │   ├── java
│   │   │   └── com/haikal/otel/AppTest.java
│── pom.xml
```

### **🔹 3. Código básico (`App.java`)**
A aplicação simplesmente imprime `Processando...` a cada 5 segundos:
```java
package com.haikal.otel;

public class App {
    public static void main(String[] args) {
        System.out.println("Aplicação Java Simples Rodando...");
        while (true) {
            try {
                Thread.sleep(5000);
                System.out.println("Processando...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Encerrando aplicação.");
                break;
            }
        }
    }
}
```

### **🔹 4. Ajustar `pom.xml` para Java 17**
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

### **🔹 5. Compilar e executar**
```sh
mvn clean package
java -jar target/otel-demo-1.0-SNAPSHOT.jar
```

---
## **📌 Etapa 2: Inspecionar métricas da JVM via JMX**
A JVM expõe métricas via **JMX (Java Management Extensions)**.

### **🔹 1. Rodar a aplicação com JMX ativado**
```sh
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -jar target/otel-demo-1.0-SNAPSHOT.jar
```

### **🔹 2. Inspecionar métricas via JConsole**
Abra o **JConsole** e conecte-se ao processo da aplicação:
```sh
jconsole
```
Verifique:
- **Uso de memória** (`java.lang:type=Memory`)
- **Threads ativas** (`java.lang:type=Threading`)
- **Coleta de Garbage Collection** (`java.lang:type=GarbageCollector`)

### **🔹 3. Inspecionar via `jcmd`**
Lista os processos Java ativos:
```sh
jcmd
```
Ver métricas da JVM (substitua `PID` pelo ID da aplicação):
```sh
jcmd PID GC.heap_info
```

---
## **📌 Etapa 3: Instrumentar a Aplicação com OpenTelemetry Agent**
Usaremos o **OpenTelemetry Java Agent** para coletar métricas automaticamente **sem modificar o código-fonte**.

### **🔹 1. Baixar o OpenTelemetry Java Agent**
```sh
curl -L -o opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
```

### **🔹 2. Rodar a aplicação com o agente**
```sh
java -javaagent:opentelemetry-javaagent.jar \
     -Dotel.exporter.otlp.metrics.endpoint=http://localhost:4318/v1/metrics \
     -Dotel.metrics.exporter=otlp \
     -Dotel.traces.exporter=none \
     -Dotel.logs.exporter=none \
     -Dotel.resource.attributes="service.name=otel-demo" \
     -Dotel.logging.level=debug \
     -jar target/otel-demo-1.0-SNAPSHOT.jar
```

📌 **O que acontece?**
- O agente captura métricas **automáticas** da JVM.
- Ele converte **métricas JMX em OTLP**.
- Ainda não temos um **backend**, então os dados não são armazenados.

---
## **📌 Etapa 4: Configurar OpenTelemetry Collector**
O **OpenTelemetry Collector** recebe métricas via **OTLP** e expõe no formato **Prometheus**.

### **🔹 1. Baixar o OpenTelemetry Collector**
**Mac Intel (x86_64):**
```sh
curl -L -o otelcol.tar.gz \
    https://github.com/open-telemetry/opentelemetry-collector-releases/releases/latest/download/otelcol-contrib_0.119.0_darwin_amd64.tar.gz
```
**Mac ARM (M1, M2, M3...):**
```sh
curl -L -o otelcol.tar.gz \
    https://github.com/open-telemetry/opentelemetry-collector-releases/releases/latest/download/otelcol-contrib_0.119.0_darwin_arm64.tar.gz
```

Descompactado e dado permissao necessaria
```sh
tar -xvzf otelcol.tar.gz
chmod +x otelcol-contrib
```

### **🔹 2. Configurar o OpenTelemetry Collector**
Crie `otel-collector-config.yaml`:
```yaml
receivers:
  otlp:
    protocols:
      grpc:
      http:

exporters:
  prometheus:
    endpoint: "0.0.0.0:9464"
    resource_to_telemetry_conversion:
      enabled: false # -false > jvm_class_count extras atributtes removed
    enable_open_metrics: false # false -> metrics in open metrics formats
    add_metric_suffixes: false # false -> jvm_class_loaded | true -> jvm_class_loaded_total

service:
  pipelines:
    metrics:
      receivers: [otlp]
      exporters: [prometheus]
```

> Mais infos sobre promtheus export (Prometheus Exporter)[https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/exporter/prometheusexporter/README.md]

### **🔹 3. Executar o Collector**
```sh
./otelcol-contrib --config otel-collector-config.yaml
```

### **🔹 4. Verificar as métricas exportadas**
```sh
curl http://localhost:9464/metrics
curl -s http://localhost:9464/metrics | grep -i "jvm_" | head -20

```
---

### **Resumo do que fizemos até aqui** 🚀

1. **Criamos uma aplicação Java simples** (sem nenhuma instrumentação) usando Maven.
2. **Rodamos a aplicação sem instrumentação** para entender seu funcionamento básico.
3. **Ativamos o JMX** para visualizar métricas padrão da JVM usando **JConsole** e **jcmd**.
4. **Rodamos a aplicação com o OpenTelemetry Agent**, permitindo a coleta automática de métricas sem modificar o código.
5. **Configuramos o OpenTelemetry Collector** para exportar as métricas no formato **Prometheus**.
6. **Validamos as métricas exportadas** com `curl`, garantindo que estavam disponíveis na porta correta.
7. **Exploramos diferentes configurações do OpenTelemetry Collector**, observando o impacto de parâmetros como:
   - `resource_to_telemetry_conversion`
   - `enable_open_metrics`
   - `add_metric_suffixes`
8. **Comparamos os formatos de saída das métricas**, analisando a presença de sufixos (`_total`, `_count`), rótulos extras e comportamento ao modificar as configurações do coletor.


Então **o agente do OpenTelemetry coleta métricas de três principais fontes quando a aplicação não tem SDK**:

1️⃣ **JMX (Java Management Extensions)** → Ele pega métricas padrão da JVM, como:
   - `jvm_memory_used_bytes`
   - `jvm_cpu_time_seconds_total`
   - `jvm_gc_duration_seconds`
   - `jvm_threads_active`

2️⃣ **Instrumentação Automática de Bibliotecas Populares** → O agente modifica o bytecode em tempo de execução para capturar métricas de algumas bibliotecas específicas, como:
   - **HTTP Clients**: Apache HTTP Client, OkHttp, HttpURLConnection
   - **JDBC**: Conexões e queries no banco
   - **Mensageria**: Kafka, RabbitMQ
   - **Frameworks Web**: Spring Boot, JAX-RS, etc.

3️⃣ **Eventos Internos da JVM** → O agente pode capturar métricas de eventos internos da JVM, como:
   - **Tempo de CPU**
   - **GC Pause Time**
   - **Uso de memória**

---

## **📌 Como isso funciona sem SDK?**
- O **JMX** já expõe as métricas nativamente, então o agente só precisa coletá-las.
- Para **bibliotecas populares**, o agente insere hooks (bytecode manipulation) para interceptar chamadas relevantes e registrar métricas automaticamente.
- **Se a biblioteca não estiver na lista de instrumentação automática**, o agente **não consegue capturar métricas dela**.

---



```sh
-Dotel.logging.level=debug
```
Isso mostrará **quais instrumentações foram aplicadas**.

📌 **Se o agente não capturar automaticamente, a solução é adicionar métricas via JMX ou implementar um SDK manualmente.**

Faz sentido agora? 🚀
📌 **Próximo passo:** Adicionar **Micrometer** e comparar os nomes das métricas geradas. 🎯


