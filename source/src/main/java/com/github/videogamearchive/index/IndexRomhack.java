package com.github.videogamearchive.index;

import com.github.videogamearchive.model.Patch;
import com.github.videogamearchive.model.Romhack;
import com.github.videogamearchive.util.CSV;

import java.util.ArrayList;
import java.util.List;

public record IndexRomhack(
        String system,
        String parent,
        String name,
        Romhack romhack) implements Comparable<IndexRomhack> {

    public static String[] headers() {
        String[] indexHeaders = new String[] {
                // Folder names info
                "Parent", "Name", "Download", "System",
                // Info
                "Name (original)", "Translated Title", "Status", "Adult", "Offensive", "Obsolete Version", "Back Catalog",
                // Provenance
                "Retrieved By", "Retrieved Date", "Source", "Notes",
                // Rom
                "Size", "CRC32", "MD5", "SHA-1",
                // Patch 1
                "Id", "Authors", "Short Authors", "Url", "Other Urls", "Version", "Release Date", "Options", "Labels",
                // Patch 2-5
        };

        List<String> indexHeadersAsList = new ArrayList<>(List.of(indexHeaders));

        for (int i = 2; i <= 5; i++) {
            indexHeadersAsList.addAll(List.of(
                    "Id (" + i + ")",
                    "Authors (" + i + ")",
                    "Short Authors (" + i + ")",
                    "Url (" + i + ")",
                    "Other Urls (" + i + ")",
                    "Version (" + i + ")",
                    "Release Date (" + i + ")",
                    "Options (" + i + ")",
                    "Labels (" + i + ")"
            ));
        }

        return indexHeadersAsList.toArray(new String[] {});
    }

    public String[] row() {
        String[] index = new String[] {
                // Folder names info
                CSV.toString(parent), CSV.toString(name), CSV.toString("https://github.com/videogame-archive/romhack-archive/raw/main/database/" + system + "/" + parent + "/" + name + "/romhack.bps"), CSV.toString(system),
                // Info
                CSV.toString(romhack.info().name()), CSV.toString(romhack.info().translatedTitle()), CSV.toString(romhack.info().status()), CSV.toString(romhack.info().adult()), CSV.toString(romhack.info().offensive()), CSV.toString(romhack.info().obsoleteVersion()), CSV.toString(romhack.info().backCatalog()),
                // Provenance
                CSV.toString(romhack.provenance().retrievedBy()), CSV.toString(romhack.provenance().retrievedDate()), CSV.toString(romhack.provenance().source()), CSV.toString(romhack.provenance().notes()),
                // Rom
                CSV.toString(romhack.rom().size()), CSV.toString(romhack.rom().crc32()), CSV.toString(romhack.rom().md5()), CSV.toString(romhack.rom().sha1()),
        };

        List<String> romAsList = new ArrayList<>(List.of(index));

        int numPatches = 0;
        for (Patch patch:romhack.patches()) {
            romAsList.add(CSV.toString(patch.id()));
            romAsList.add(CSV.toString(patch.authors()));
            romAsList.add(CSV.toString(patch.shortAuthors()));
            romAsList.add(CSV.toString(patch.url()));
            romAsList.add(CSV.toString(patch.otherUrls()));
            romAsList.add(CSV.toString(patch.version()));
            romAsList.add(CSV.toString(patch.releaseDate()));
            romAsList.add(CSV.toString(patch.options()));
            romAsList.add(CSV.toString(patch.labels()));
            numPatches++;
        }

        for (int i = numPatches; i <= 5; i++) {
            romAsList.add("");
            romAsList.add("");
            romAsList.add("");
            romAsList.add("");
            romAsList.add("");
            romAsList.add("");
            romAsList.add("");
            romAsList.add("");
        }

        return romAsList.toArray(new String[] {});
    }
    @Override
    public int compareTo(IndexRomhack o) {
        return name.compareTo(o.name);
    }
}