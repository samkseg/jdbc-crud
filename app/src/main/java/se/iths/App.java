package se.iths;

import se.iths.persistency.dao.AlbumDAO;
import se.iths.persistency.dao.ArtistDAO;
import se.iths.persistency.dao.TrackDAO;
import se.iths.persistency.model.Album;
import se.iths.persistency.model.Artist;
import se.iths.persistency.model.Track;

import java.sql.*;
import java.util.*;

public class App {

  protected static final ArtistDAO artistDAO = new ArtistDAO();
  protected static final AlbumDAO albumDAO = new AlbumDAO();
  protected static final TrackDAO trackDAO = new TrackDAO();
  protected static HashMap<Long, Artist> artists = new HashMap<>();
  public static void main(String[] args) {
    App app = new App();
    try {

      app.load();
      printList();

    } catch (SQLException e) {
      System.err.printf("Error reading database %s%n", e);
    }
  }

  public void load() throws SQLException {
    loadArtistsAlbumsTracks();
  }

  public static void printList() {
    for(Artist artist : artists.values()){
      System.out.println(artist);
    }
  }

  //  CREATE - add new objects to database
  protected static Optional<Artist> addArtist(String name) throws SQLException {
    Optional<Artist> artist = artistDAO.create(new Artist(name));
    if (artist.isPresent()) {
      artists.put(artist.get().getArtistId(), artist.get());
      return artist;
    }
    return Optional.empty();
  }

  protected static Optional<Album> addAlbum(long artistId, String title) throws SQLException {
    Optional<Artist> artist = findArtistById(artistId);
    if (artist.isPresent()) {
      Optional<Album> album = albumDAO.create(new Album(title, artist.get().getArtistId()));
      album.ifPresent(a -> artist.get().add(a));
      return album;
    }
    return Optional.empty();
  }

  protected static Optional<Track> addTrack(long albumId, String name) throws SQLException {
    Optional<Album> album = findAlbumById(albumId);
    if (album.isPresent()) {
      Optional<Artist> artist = findArtistById(album.get().getArtistId());
      if (artist.isPresent()) {
        Optional<Track> track = trackDAO.create(new Track(name, album.get().getAlbumId()));
        track.ifPresent(t -> album.get().add(t));
        return track;
      }
    }
    return Optional.empty();
  }

  // READ - load all, find all & get new objects by id from database
  private void loadArtistsAlbumsTracks() throws SQLException {
    artists.clear();
    for (Artist artist : artistDAO.findAll()) {
      artists.put(artist.getArtistId(), artist);
      Collection<Album> artistAlbums = albumDAO.findByArtistId(artist.getArtistId());
      artist.addAll(artistAlbums);
      for (Album album : artistAlbums) {
        Collection<Track> albumTracks = trackDAO.findByAlbumId(album.getAlbumId());
        album.addAll(albumTracks);
      }
    }
  }

  protected static Collection<Album> findAllAlbums() throws SQLException {
    artists.clear();
    Collection<Album> albums = new ArrayList<>();
    for (Artist artist : artistDAO.findAll()) {
      artists.put(artist.getArtistId(), artist);
      Collection<Album> artistAlbums = albumDAO.findByArtistId(artist.getArtistId());
      artist.addAll(artistAlbums);
      albums.addAll(artistAlbums);
      for (Album album : artistAlbums) {
        Collection<Track> albumTracks = trackDAO.findByAlbumId(album.getAlbumId());
        album.addAll(albumTracks);
      }
    }
    return albums;
  }

  protected static Collection<Track> findAllTracks() throws SQLException {
    artists.clear();
    Collection<Track> tracks = new ArrayList<>();
    for (Artist artist : artistDAO.findAll()) {
      artists.put(artist.getArtistId(), artist);
      Collection<Album> artistAlbums = albumDAO.findByArtistId(artist.getArtistId());
      artist.addAll(artistAlbums);
      for (Album album : artistAlbums) {
        Collection<Track> albumTracks = trackDAO.findByAlbumId(album.getAlbumId());
        album.addAll(albumTracks);
        tracks.addAll(albumTracks);
      }
    }
    return tracks;
  }

  protected static Optional<Artist> findArtistById(long artistId) throws SQLException {
    Optional<Artist> artist = artistDAO.findById(artistId);
    if (artist.isPresent()) {
      Collection<Album> artistAlbums = albumDAO.findByArtistId(artistId);
      for (Album album : artistAlbums) {
        Collection<Track> albumTracks = trackDAO.findByAlbumId(album.getAlbumId());
        album.addAll(albumTracks);
      }
      artist.get().addAll(artistAlbums);
      artists.replace(artistId, artist.get());
      return artist;
    }
    return Optional.empty();
  }

  protected static Optional<Album> findAlbumById(long albumId) throws SQLException {
    Optional<Album> album = albumDAO.findById(albumId);
    if (album.isPresent()) {
      Optional<Artist> artist = findArtistById(album.get().getArtistId());
      if (artist.isPresent()) {
        Collection<Track> albumTracks = trackDAO.findByAlbumId(albumId);
        album.get().addAll(albumTracks);
        artist.get().replace(album.get());
        artists.replace(artist.get().getArtistId(), artist.get());
        return album;
      }
    }
    return Optional.empty();
  }

  protected static Optional<Track> findTrackById(long trackId) throws SQLException {
    Optional<Track> track = trackDAO.findById(trackId);
    if (track.isPresent()) {
      Optional<Album> album = findAlbumById(track.get().getAlbumId());
      if (album.isPresent()) {
        Optional<Artist> artist = findArtistById(album.get().getArtistId());
        if (artist.isPresent()) {
          album.get().replace(track.get());
          artist.get().replace(album.get());
          artists.replace(artist.get().getArtistId(), artist.get());
        }
        return track;
      }
    }
    return Optional.empty();
  }

  // UPDATE - renames objects and pushes to database
  protected static Optional<Artist> updateArtist(long artistId, String newName) throws SQLException {
    Optional<Artist> artist = findArtistById(artistId);
    if (artist.isPresent()) {
      artist.get().setName(newName);
      artistDAO.update(artist.get());
      return artist;
    }
    return Optional.empty();
  }

  protected static Optional<Album> updateAlbum(long albumId, String newTitle) throws SQLException {
    Optional<Album> album = findAlbumById(albumId);
    if (album.isPresent()) {
      album.get().setTitle(newTitle);
      albumDAO.update(album.get());
      return album;
    }
    return Optional.empty();
  }

  protected static Optional<Track> updateTrack(long trackId, String newName) throws SQLException {
    Optional<Track> track = findTrackById(trackId);
    if (track.isPresent()) {
      track.get().setName(newName);
      trackDAO.update(track.get());
      return track;
    }
    return Optional.empty();
  }

  // DELETE - removes objects from database
  public static boolean deleteArtist(long artistId) throws SQLException {
    Optional<Artist> artist = findArtistById(artistId);
    int countAlbum = 0;
    if (artist.isPresent()) {
      for (Album album : artist.get().getAlbums()) {
        int countTrack = 0;
        for (Track track : album.getTracks()) {
          if (trackDAO.delete(track)) countTrack++;
        }
        if (countTrack == album.getTracks().size()) album.removeAll();
        if (albumDAO.delete(album)) countAlbum++;
      }
      if (countAlbum == artist.get().getAlbums().size()) artist.get().removeAll();
      boolean deletedFromDB = artistDAO.delete(artist.get());
      if (deletedFromDB) {
        artists.remove(artist.get().getArtistId());
        return true;
      }
    }
    return false;
  }

  protected static boolean deleteAlbum(long albumId) throws SQLException {
    Optional<Album> album = findAlbumById(albumId);
    if (album.isPresent()) {
      Optional<Artist> artist = findArtistById(album.get().getArtistId());
      if(artist.isPresent()) {
        int counter = 0;
        for (Track track : album.get().getTracks()) {
          if (trackDAO.delete(track)) counter++;
        }
        if (counter == album.get().getTracks().size()) album.get().removeAll();
        boolean deletedFromDB = albumDAO.delete(album.get());
        if (deletedFromDB) {
          artist.get().remove(album.get());
          return true;
        }
      }
    }
    return false;
  }

  protected static boolean deleteTrack(long trackId) throws SQLException {
    Optional<Track> track = findTrackById(trackId);
    if (track.isPresent()) {
      Optional<Album> album = findAlbumById(track.get().getAlbumId());
      if (album.isPresent()) {
        boolean deletedFromDB = trackDAO.delete(track.get());
        if (deletedFromDB) {
          album.get().remove(track.get());
          return true;
        }
      }
    }
    return false;
  }
}