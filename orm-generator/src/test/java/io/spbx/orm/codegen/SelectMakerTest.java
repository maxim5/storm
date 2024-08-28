package io.spbx.orm.codegen;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static io.spbx.orm.api.ReadFollow.*;
import static io.spbx.orm.arch.factory.TestingArch.buildTableArch;
import static io.spbx.orm.codegen.AssertSnippet.assertThatSql;
import static io.spbx.util.testing.TestingBasics.listOf;

public class SelectMakerTest {
    @Test
    public void one_level() {
        record User(int userId, String name) {}
        record Song(ForeignInt<User> author) {}

        TableArch tableArch = buildTableArch(Song.class, listOf(User.class));
        SelectMaker selectMaker = new SelectMaker(tableArch);

        assertThat(selectMaker)
            .assertNoFollow("""
                SELECT author_id
                FROM song
                """)
            .assertFollowOneOrMore("""
                SELECT song.author_id, user.user_id, user.name
                FROM song
                LEFT JOIN user ON song.author_id = user.user_id
                """);
    }

    @Test
    public void two_levels() {
        record User(int userId, String name) {}
        record Song(int songId, ForeignInt<User> author) {}
        record Single(ForeignInt<Song> hitSong) {}

        TableArch tableArch = buildTableArch(Single.class, listOf(User.class, Song.class));
        SelectMaker selectMaker = new SelectMaker(tableArch);

        assertThat(selectMaker)
            .assertNoFollow("""
                SELECT hit_song_id
                FROM single
                """)
            .assertFollowOneLevel("""
                SELECT single.hit_song_id, song.song_id, song.author_id
                FROM single
                LEFT JOIN song ON single.hit_song_id = song.song_id
                """)
            .assertFollowAll("""
                SELECT single.hit_song_id, song.song_id, song.author_id, user.user_id, user.name
                FROM single
                LEFT JOIN song ON single.hit_song_id = song.song_id
                LEFT JOIN user ON song.author_id = user.user_id
                """);
    }

    @CheckReturnValue
    private static @NotNull SelectMakerSubject assertThat(@NotNull SelectMaker selectMaker) {
        return new SelectMakerSubject(selectMaker);
    }

    @CanIgnoreReturnValue
    private record SelectMakerSubject(@NotNull SelectMaker selectMaker) {
        public @NotNull SelectMakerSubject assertNoFollow(@NotNull String expected) {
            assertThatSql(selectMaker.make(NO_FOLLOW)).matches(expected);
            return this;
        }

        public @NotNull SelectMakerSubject assertFollowOneLevel(@NotNull String expected) {
            assertThatSql(selectMaker.make(FOLLOW_ONE_LEVEL)).matches(expected);
            return this;
        }

        public @NotNull SelectMakerSubject assertFollowAll(@NotNull String expected) {
            assertThatSql(selectMaker.make(FOLLOW_ALL)).matches(expected);
            return this;
        }

        public @NotNull SelectMakerSubject assertFollowOneOrMore(@NotNull String expected) {
            return assertFollowOneLevel(expected).assertFollowAll(expected);
        }
    }
}
