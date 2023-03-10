package se.iths;

import org.junit.jupiter.api.*;
import se.iths.persistency.model.Album;
import se.iths.persistency.model.Artist;
import se.iths.persistency.model.Track;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppTest {
    static App app = new App();

    @BeforeAll
    public static void load() throws SQLException {
        app.load();
    }

    @AfterAll
    public static void print() {
        app.printList();
        System.out.println("Expected:\n277: TestArtist2\n\tAlbums:\n\t\t350: TestAlbum3\n\t\t\tTracks:\n\t\t\t\t3504: NewTestTrack\n");
    }

    //  CREATE - add new objects to database
    @Order(1)
    @Test
    void shouldCreateArtists() throws SQLException {
        Optional<Artist> artist1 = app.addArtist("TestArtist1");
        Optional<Artist> artist2 = app.addArtist("TestArtist2");
        Optional<Artist> artist3 = app.addArtist("TestArtist3");
        Optional<Artist> artist4 = app.addArtist("TestArtist4");

        assertEquals("TestArtist1", artist1.get().getName());
        assertEquals("TestArtist2", artist2.get().getName());
        assertEquals("TestArtist3", artist3.get().getName());
        assertEquals("TestArtist4", artist4.get().getName());
    }

    @Order(2)
    @Test
    void shouldCreateAlbums() throws SQLException {
        Optional<Album> album1 = app.addAlbum(276, "TestAlbum1");
        Optional<Album> album2 = app.addAlbum(276, "TestAlbum2");
        Optional<Album> album3 = app.addAlbum(277, "TestAlbum3");
        Optional<Album> album4 = app.addAlbum(277, "TestAlbum4");
        Optional<Album> album5 = app.addAlbum(278, "TestAlbum5");
        Optional<Album> album6 = app.addAlbum(279, "TestAlbum6");


        assertEquals("TestAlbum1", album1.get().getTitle());
        assertEquals("TestAlbum2", album2.get().getTitle());
        assertEquals("TestAlbum3", album3.get().getTitle());
        assertEquals("TestAlbum4", album4.get().getTitle());
        assertEquals("TestAlbum5", album5.get().getTitle());
        assertEquals("TestAlbum6", album6.get().getTitle());
    }

    @Order(3)
    @Test
    void shouldCreateTracks() throws SQLException {
        Optional<Track> track1 = app.addTrack(350, "TestTrack1");
        Optional<Track> track2 = app.addTrack(350, "TestTrack2");
        Optional<Track> track3 = app.addTrack(351, "TestTrack3");
        Optional<Track> track4 = app.addTrack(352, "TestTrack4");
        Optional<Track> track5 = app.addTrack(353, "TestTrack5");

        assertEquals("TestTrack1", track1.get().getName());
        assertEquals("TestTrack2", track2.get().getName());
        assertEquals("TestTrack3", track3.get().getName());
        assertEquals("TestTrack4", track4.get().getName());
        assertEquals("TestTrack5", track5.get().getName());
    }

    // READ - load all, find all & get new objects by id from database
    @Order(4)
    @Test
    void shouldFindArtist() throws SQLException {
        Optional<Artist> testGetArtistFromDatabase = app.findArtistById(277);

        assertEquals("TestArtist2", testGetArtistFromDatabase.get().getName());
    }

    @Order(5)
    @Test
    void shouldFindAlbum() throws SQLException {
        Optional<Album> testGetAlbumFromDatabase = app.findAlbumById(349);

        assertEquals("TestAlbum2", testGetAlbumFromDatabase.get().getTitle());
    }

    @Order(6)
    @Test
    void shouldFindTrack() throws SQLException {
        Optional<Track> testGetTrackFromDatabase = app.findTrackById(3505);

        assertEquals("TestTrack2", testGetTrackFromDatabase.get().getName());

    }

    @Order(7)
    @Test
    void shouldFindAllAlbums() throws SQLException {
        Collection<Album> albums = new ArrayList<>();
        for (Artist artist :  app.artists.values()) albums.addAll(artist.getAlbums());

        Collection<Album> albums2 = app.findAllAlbums();

        assertTrue(albums.size() == albums2.size());
    }

    @Order(8)
    @Test
    void shouldFindAllTracks() throws SQLException {
        Collection<Track> tracks = new ArrayList<>();
        for (Artist artist :  app.artists.values()) {
            for (Album album : app.artists.get(artist.getArtistId()).getAlbums())
                tracks.addAll(album.getTracks());
        }

        Collection<Track> tracks2 = app.findAllTracks();

        assertTrue(tracks.size() == tracks2.size());
    }

    // UPDATE - renames objects and pushes to database
    @Order(9)
    @Test
    void shouldUpdateArtist() throws SQLException {
        app.updateArtist(276, "NewTestArtist");

        Optional<Artist> artist = app.findArtistById(276);
        assertEquals("NewTestArtist", artist.get().getName());
    }

    @Order(10)
    @Test
    void shouldUpdateAlbum() throws SQLException {
        app.updateAlbum(348, "NewTestAlbum");

        Optional<Album> album = app.findAlbumById(348);
        assertEquals("NewTestAlbum", album.get().getTitle());
    }

    @Order(11)
    @Test
    void shouldUpdateTrack() throws SQLException {
        app.updateTrack(3504, "NewTestTrack");

        Optional<Track> track = app.findTrackById(3504);
        assertEquals("NewTestTrack", track.get().getName());
    }

    // DELETE - removes objects from database
    @Order(12)
    @Test
    void shouldDeleteAlbum() throws SQLException {
        assertTrue(app.deleteAlbum(348));

        Optional<Album> album1 = app.findAlbumById(348);
        assertTrue(album1.isEmpty());

        assertTrue(app.deleteAlbum(351));

        Optional<Album> album2 = app.findAlbumById(351);
        assertTrue(album2.isEmpty());
    }

    @Order(13)
    @Test
    void shouldDeleteArtist() throws SQLException {
        assertTrue(app.deleteArtist(276));

        Optional<Artist> artist1 = app.findArtistById(276);
        assertTrue(artist1.isEmpty());

        assertTrue(app.deleteArtist(278));

        Optional<Artist> artist2 = app.findArtistById(278);
        assertTrue(artist2.isEmpty());
    }

    @Order(14)
    @Test
    void shouldDeleteTrack() throws SQLException {
        assertTrue(app.deleteTrack(3505));

        Optional<Track> track = app.findTrackById(3505);
        assertTrue(track.isEmpty());
    }

    @Order(15)
    @Test
    void shouldDeleteArtistWithAlbumsAndTracks() throws SQLException {
        assertTrue(app.deleteArtist(279));

        Collection<Album> allAlbums = app.findAllAlbums();
        Collection<Album> albums = new ArrayList<>();
        Collection<Track> tracks = new ArrayList<>();
        for (Album album : allAlbums) {
            if (album.getArtistId() == 279) {
                albums.add(album);
                tracks.addAll(album.getTracks());
            }
        }
        Optional<Artist> artist = app.findArtistById(279);
        assertTrue(tracks.isEmpty());
        assertTrue(albums.isEmpty());
        assertTrue(artist.isEmpty());
    }

    // Optional - prevent empty object insertion to database
    @Order(16)
    @Test
    void shouldNotCreateAlbumWithoutParent () throws SQLException {
        Optional<Album> TestAddAlbumWithoutArtistId = app.addAlbum(0, "TestTitle");

        assertTrue(TestAddAlbumWithoutArtistId.isEmpty());
    }

    @Order(17)
    @Test
    void shouldNotCreateTrackWithoutParent () throws SQLException {
        Optional<Track> TestAddTrackWithoutAlbumId = app.addTrack(0, "TestTrack");

        assertTrue(TestAddTrackWithoutAlbumId.isEmpty());
    }

    @Order(18)
    @Test
    void shouldFindEmptyArtist () throws SQLException {
        Optional<Artist> testGetEmptyArtist = app.findArtistById(0);

        assertTrue(testGetEmptyArtist.isEmpty());
    }

    @Order(19)
    @Test
    void shouldFindEmptyAlbum () throws SQLException {
        Optional<Album> testGetEmptyAlbum = app.findAlbumById(0);

        assertTrue(testGetEmptyAlbum.isEmpty());
    }

    @Order(20)
    @Test
    void shouldFindEmptyTrack () throws SQLException {
        Optional<Track> testGetEmptyTrack = app.findTrackById(0);

        assertTrue(testGetEmptyTrack.isEmpty());
    }

    @Order(21)
    @Test
    void shouldNotUpdateEmptyArtist () throws SQLException {
        Optional<Artist> testUpdateEmptyArtist = app.updateArtist(0, "NewTestArtist");

        assertTrue(testUpdateEmptyArtist.isEmpty());
    }

    @Order(22)
    @Test
    void shouldNotUpdateEmptyAlbum () throws SQLException {
        Optional<Album> testUpdateEmptyAlbum = app.updateAlbum(0, "NewTestAlbum");

        assertTrue(testUpdateEmptyAlbum.isEmpty());
    }

    @Order(23)
    @Test
    void shouldNotUpdateEmptyTrack () throws SQLException {
        Optional<Track> testUpdateEmptyTrack = app.updateTrack(0, "NewTestTrack2");

        assertTrue(testUpdateEmptyTrack.isEmpty());
    }

    @Order(24)
    @Test
    void shouldNotDeleteEmptyArtist () throws SQLException {
        boolean deletedArtist = app.deleteArtist(0);

        assertFalse(deletedArtist);
    }

    @Order(25)
    @Test
    void shouldNotDeleteEmptyAlbum () throws SQLException {
        boolean deletedAlbum = app.deleteAlbum(0);

        assertFalse(deletedAlbum);
    }

    @Order(26)
    @Test
    void shouldNotDeleteEmptyTrack () throws SQLException {
        boolean deletedTrack = app.deleteTrack(0);

        assertFalse(deletedTrack);
    }
}
