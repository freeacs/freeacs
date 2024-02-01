package com.github.freeacs.ws;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = WebserviceIntegrationTest.DataSourceInitializer.class)
public class WebserviceIntegrationTest implements AbstractMySqlIntegrationTest {

  public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      MysqlDataSourceInitializer.initialize(mysql, applicationContext);
    }
  }

  @Autowired private TestRestTemplate restTemplate;

  @Test
  public void testWsdlPage() {
    String body = this.restTemplate.getForObject("/ws/acs.wsdl", String.class);
    assertThat(body).contains("GetUnittypesRequest");
  }

  @Test
  public void testKick() {
    String xml =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            + "xmlns:x.=\"http://xml.ws.freeacs.github.com/\">\n"
            + "   <soapenv:Header/>\n"
            + "   <soapenv:Body>\n"
            + "      <x.:KickUnitRequest>\n"
            + "         <x.:login>\n"
            + "            <x.:username>admin</x.:username>\n"
            + "            <!--Optional:-->\n"
            + "            <x.:password>freeacs</x.:password>\n"
            + "         </x.:login>\n"
            + "         <x.:unitId>test</x.:unitId>\n"
            + "      </x.:KickUnitRequest>\n"
            + "   </soapenv:Body>\n"
            + "</soapenv:Envelope>";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_XML);
    HttpEntity<String> httpEntity = new HttpEntity<>(xml, headers);
    ResponseEntity<String> body =
        this.restTemplate.postForEntity("/ws/acs", httpEntity, String.class);
    assertThat(body.getBody()).contains("KickUnitResponse");
  }
}
