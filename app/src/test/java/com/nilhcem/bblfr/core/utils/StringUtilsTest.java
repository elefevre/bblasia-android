package com.nilhcem.bblfr.core.utils;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.nilhcem.bblfr.BBLRobolectricTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import edu.emory.mathcs.backport.java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BBLRobolectricTestRunner.class)
public class StringUtilsTest {

    @Test
    public void should_append_two_strings() {
        // Given
        String first = "Hello, ";
        String second = "World!";

        // When
        String result = StringUtils.appendOptional(first, second);

        // Then
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    public void should_not_append_string_if_one_is_null() {
        // Given
        String expected = "hello";

        // When
        String res1 = StringUtils.appendOptional(null, expected);
        String res2 = StringUtils.appendOptional(expected, null);

        // Then
        assertThat(res1).isEqualTo(res2).isEqualTo(expected);
    }

    @Test
    public void should_append_with_a_separator() {
        // Given
        String first = "Hello";
        String second = "World!";
        String separator = ", ";

        // When
        String result = StringUtils.appendOptional(first, second, separator);

        // Then
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    public void should_append_list_of_string_with_separator() {
        // Given
        List<String> entries = Arrays.asList(new String[]{"1", "2", "3"});
        String separator = ":";

        // When
        String result = StringUtils.strJoin(entries, separator);

        // Then
        assertThat(result).isEqualTo("1:2:3");
    }

    @Test
    public void should_append_list_of_string_without_separator_if_null() {
        // Given
        List<String> entries = Arrays.asList(new String[]{"1", "2", "3"});
        String separator = null;

        // When
        String result = StringUtils.strJoin(entries, separator);

        // Then
        assertThat(result).isEqualTo("123");
    }

    @Test
    public void should_return_empty_string_if_nothing_to_join() {
        // Given
        List<String> entries = null;
        String separator = ":";

        // When
        String result = StringUtils.strJoin(entries, separator);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void should_add_line_separator_to_non_empty_spannable_string_builder() {
        // Given
        SpannableStringBuilder sb = new SpannableStringBuilder("Hello");

        // When
        StringUtils.addLineSeparator(sb);

        // Then
        assertThat(sb.toString()).isEqualTo("Hello" + StringUtils.LINE_SEPARATOR);
    }

    @Test
    public void should_not_add_line_separator_to_empty_spannable_string_builder() {
        // Given
        SpannableStringBuilder sb = new SpannableStringBuilder();

        // When
        StringUtils.addLineSeparator(sb);

        // Then
        assertThat(sb.toString()).isEmpty();
    }

    @Test
    public void should_add_line_separator_to_empty_spannable_string_builder_if_force_arg_is_true() {
        // Given
        SpannableStringBuilder sb = new SpannableStringBuilder();

        // When
        StringUtils.addLineSeparator(sb, true);

        // Then
        assertThat(sb.toString()).isEqualTo(StringUtils.LINE_SEPARATOR);
    }

    @Test
    public void should_create_spanned_html_link() {
        // Given
        String title = "website";
        String href = "http://www.nilhcem.com";

        // When
        Spanned htmlLink = StringUtils.createSpannedHtmlLink(title, href);

        // Then
        assertThat(htmlLink).isEqualTo(Html.fromHtml("<a href=\"" + href + "\">" + title));
    }
}
