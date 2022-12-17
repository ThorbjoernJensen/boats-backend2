package rest;

import dtos.BoatDTO;
import dtos.HarbourDTO;
import dtos.OwnerDTO;
import entities.*;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static io.restassured.RestAssured.given;

public class APIResourceTest {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    private static Owner o1, o2, o3;
    private static OwnerDTO o1DTO, o2DTO, o3DTO;
    private static Harbour h1, h2, h3;
    private static HarbourDTO h1DTO, h2DTO, h3DTO;
    private static Boat b1, b2, b3;
    private static BoatDTO b1DTO, b2DTO, b3DTO;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();

        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();

        httpServer.shutdownNow();
    }

    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            em.createQuery("delete from Boat").executeUpdate();
            em.createQuery("delete from Harbour").executeUpdate();
            em.createQuery("delete from Owner").executeUpdate();
            em.createQuery("delete from User").executeUpdate();
            em.createQuery("delete from Role").executeUpdate();

            Role userRole = new Role("user");
            Role adminRole = new Role("admin");
            User user = new User("user", "test1");
            user.addRole(userRole);
            User admin = new User("admin", "test2");
            admin.addRole(adminRole);
            User both = new User("user_admin", "test3");
            both.addRole(userRole);
            both.addRole(adminRole);
            em.persist(userRole);
            em.persist(adminRole);
            em.persist(user);
            em.persist(admin);
            em.persist(both);


            Owner o1 = new Owner("Skipper Bænt", "Persillehaven 40", "38383838");
            Owner o2 = new Owner("Skipper Niels", "Persillehaven 42", "39393939");
            Owner o3 = new Owner("Skipper Bente", "Persillehaven 38", "40404040");

            Harbour h1 = new Harbour("Melsted Havn", "Melsted byvej", 8);
            Harbour h2 = new Harbour("Nexø Havn", "Hovedvejen", 14);
            Harbour h3 = new Harbour("Aakirkeby Havn", "Melsted byvej", 32);

            Boat b1 = new Boat("Boatmaster", "speeder", "Martha", "https://img.fruugo.com/product/8/58/278398588_max.jpg");
            Boat b2 = new Boat("Das Boot", "submarine", "Aase", "https://cdn.shopify.com/s/files/1/0626/0562/3537/products/31S6ddXfLmL.jpg?v=1659358008");
            Boat b3 = new Boat("Hanger", "supersize", "King Lincoln", "https://upload.wikimedia.org/wikipedia/commons/2/2d/USS_Nimitz_%28CVN-68%29.jpg");

            b1.addOwner(o1);
            b2.addOwner(o1);
            b2.addOwner(o2);
            b3.addOwner(o3);
            b3.addOwner(o3);

            h1.addBoat(b1);
            h3.addBoat(b2);
            h3.addBoat(b3);

            em.persist(o1);
            em.persist(o2);
            em.persist(o3);
            em.persist(b1);
            em.persist(b2);
            em.persist(b3);
            em.persist(h1);
            em.persist(h2);
            em.persist(h3);
            em.getTransaction().commit();

            o1DTO = new OwnerDTO(o1);
            o2DTO = new OwnerDTO(o2);
            o1DTO = new OwnerDTO(o3);
            h1DTO = new HarbourDTO(h1);
            h2DTO = new HarbourDTO(h2);
            h3DTO = new HarbourDTO(h3);
            b1DTO = new BoatDTO(b1);
            b2DTO = new BoatDTO(b2);
            b3DTO = new BoatDTO(b3);

        } finally {
            em.close();
        }

    }

    private static void login(String username, String password) {
        String json = String.format("{username: \"%s\", password: \"%s\"}", username, password);
        securityToken = given()
                .contentType("application/json")
                .body(json)
                .when().post("/login")
                .then()
                .extract().path("token");
    }

    private void logOut() {
        securityToken = null;
    }

    private static String securityToken;


    @Test
    public void testAPIResourceIsResponding() {
        given().when().get("/boat").then().statusCode(200);
    }
    @Test
    public void testUserResourceIsResponding() {
        given().when().get("/user").then().statusCode(200);
    }



}