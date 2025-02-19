package com.github.videogamearchive.database.md;

import com.github.videogamearchive.database.DatabaseVisitor;
import com.github.videogamearchive.database.DatabaseWalker;
import com.github.videogamearchive.database.ExtendedRelease;
import com.github.videogamearchive.model.*;
import com.github.videogamearchive.model.json.ReleaseMapper;
import com.github.videogamearchive.util.CSV;
import fun.mingshan.markdown4j.Markdown;
import fun.mingshan.markdown4j.type.block.*;
import fun.mingshan.markdown4j.type.element.ImageElement;
import fun.mingshan.markdown4j.type.element.UrlElement;
import fun.mingshan.markdown4j.writer.MdWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MarkdownCreator {

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 2) {
            help();
        } else {
            boolean validate;
            if (args.length == 2) {
                validate = args[1].equals("--validate");
            } else {
                validate = false;
            }
            File root = new File(args[0]);
            if (root.exists() && root.isDirectory()) {
                CacheDatabase cacheDB = new CacheDatabase(validate);
                DatabaseWalker.processDatabase(root, cacheDB);
                generatePages(cacheDB);
            } else {
                help();
            }
        }
    }

    private static void help() {
        System.out.println("usage: ");
        System.out.println("\t\t java -jar page-creator.jar \"database\" [--validate]");
    }

    private static class CacheDatabase implements DatabaseVisitor {
        // Systems Page - List Systems [System Name]
        // docs/database/index.html
        //

        // System Page - List - Games / Files [Parent Name, File Name]
        // docs/database/system/id/index.html
        private final Map<Long, List<ExtendedRelease>> filesBySystem = new HashMap<>();
        private final Map<Long, System_> systemById = new HashMap<>();
        private final Map<Long, List<String>> systemFolderNamesById = new HashMap<>();

        // Game Page - List - Games / Files [Parent Name, File Name]
        // docs/database/game/id/index.html
        private final Map<Long, List<ExtendedRelease>> filesByGame = new HashMap<>();
        private final Map<Long, Game> gameById = new HashMap<>();
        private final Map<Long, List<String>> parentFolderNamesById = new HashMap<>();

        // Patch Page - List - Games / Files [Parent Name, File Name]
        // docs/database/patch/id/index.html
        private final Map<Long, List<ExtendedRelease>> filesByPatch = new HashMap<>();
        private final Map<Long, Hack> patchById = new HashMap<>();

        // File Page [All Fields]
        // docs/database/file/id/index.html
        private final Map<Long, ExtendedRelease> filesById = new HashMap<>();

        private boolean validate;
        public CacheDatabase(boolean validate) {
            this.validate = validate;
        }

        public boolean validate() {
            return validate;
        }

        @Override
        public void walk(File identifiableFolder, Identifiable identifiable) {
            if (identifiable instanceof System_) {
                System_ system = (System_) identifiable;
                systemById.put(system.id(), system);
                List<String> names = systemFolderNamesById.get(system.id());
                if (names == null) {
                    names = new ArrayList<>();
                    systemFolderNamesById.put(system.id(), names);
                }
                names.add(identifiableFolder.getName());
            } else if (identifiable instanceof Game) {
                Game game = (Game) identifiable;
                gameById.put(game.id(), game);
                List<String> names = systemFolderNamesById.get(game.id());
                if (names == null) {
                    names = new ArrayList<>();
                    parentFolderNamesById.put(game.id(), names);
                }
                names.add(identifiableFolder.getName());
            } else if (identifiable instanceof ExtendedRelease) {
                ExtendedRelease indexRomhack = (ExtendedRelease) identifiable;
                filesById.put(indexRomhack.romhack().id(), indexRomhack);

                List<ExtendedRelease> filesBySystemList = filesBySystem.get(indexRomhack.system().id());
                if (filesBySystemList == null) {
                    filesBySystemList = new ArrayList<>();
                    filesBySystem.put(indexRomhack.system().id(), filesBySystemList);
                }
                filesBySystemList.add(indexRomhack);

                List<ExtendedRelease> filesByGameList = filesByGame.get(indexRomhack.game().id());
                if (filesByGameList == null) {
                    filesByGameList = new ArrayList<>();
                    filesByGame.put(indexRomhack.game().id(), filesByGameList);
                }
                filesByGameList.add(indexRomhack);

                for (Hack patch:indexRomhack.romhack().hacks()) {
                    patchById.put(patch.id(), patch);

                    List<ExtendedRelease> filesByPatchList = filesByPatch.get(patch.id());
                    if (filesByPatchList == null) {
                        filesByPatchList = new ArrayList<>();
                        filesByPatch.put(patch.id(), filesByPatchList);
                    }
                    filesByPatchList.add(indexRomhack);
                }
            }
        }

        public Map<Long, List<ExtendedRelease>> getFilesBySystem() {
            return filesBySystem;
        }

        public Map<Long, System_> getSystemById() {
            return systemById;
        }

        public Map<Long, List<ExtendedRelease>> getFilesByGame() {
            return filesByGame;
        }

        public Map<Long, Game> getGameById() {
            return gameById;
        }

        public Map<Long, List<ExtendedRelease>> getFilesByPatch() {
            return filesByPatch;
        }

        public Map<Long, Hack> getPatchById() {
            return patchById;
        }

        public Map<Long, ExtendedRelease> getFilesById() {
            return filesById;
        }
    }

    private static void generatePages(CacheDatabase cacheDB) throws IOException, ReflectiveOperationException {
        generateMainIndexPage(cacheDB);
        generateSystemPage(cacheDB);
        generateGamePage(cacheDB);
        generatePatchPage(cacheDB);
        generateFilePage(cacheDB);
    }

    //
    // General Markdown ELements
    //
    private static TitleBlock getTitle(String title) {
        return TitleBlock.builder().level(TitleBlock.Level.FIRST).content(title).build();
    }

    private static StringBlock getBrand(int level) {
        String path = "brand/videogame-archive-(alt).png";
        while (level > 0) {
            path = "../" + path;
            level--;
        }
        return ImageElement.builder()
                .imageUrl(path)
                .build().toBlock();
    }
    private static void write(Path path, int level, String title, Block content) throws IOException {
        Markdown markdown = Markdown.builder()
                .name(path.toString())
                .block(getBrand(level))
                .block(StringBlock.builder().content("\n").build())
                .block(getTitle(title))
                .block(StringBlock.builder().content("\n").build())
                .block(content)
                .build();
        MdWriter.write(markdown);
    }

    private static TableBlock getRomhacksTable(List<ExtendedRelease> romhacks) {
        List<TableBlock.TableRow> rows = new ArrayList<>();
        if (romhacks != null) {
            for (ExtendedRelease romhack : romhacks) {
                List<String> rowValue = new ArrayList<>();
                UrlElement element1 = UrlElement.builder().tips(romhack.parentFolderName()).url("../../game/" + romhack.game().id() + "/index.md").build();
                rowValue.add(element1.toMd());
                UrlElement element2 = UrlElement.builder().tips(romhack.romhackFolderName()).url("../../file/" + romhack.romhack().id() + "/index.md").build();
                rowValue.add(element2.toMd());
                TableBlock.TableRow row = new TableBlock.TableRow();
                row.setRows(rowValue);
                rows.add(row);
            }
        }
        TableBlock table = TableBlock.builder().titles(List.of("Game", "Romhack")).rows(rows).build();
        return table;
    }

    //
    // Pages
    //

    private static void generateFilePage(CacheDatabase cacheDB) throws IOException, ReflectiveOperationException {
        for (long id:cacheDB.getFilesById().keySet()) {
            ExtendedRelease indexRomhack = cacheDB.getFilesById().get(id);
            Path path = Path.of("../docs/database/file/" + id + "/index");
            Files.createDirectories(path.getParent());
            write(path, 3, "File: " + indexRomhack.romhackFolderName(), CodeBlock.builder().language("json").content(new ReleaseMapper().write(indexRomhack.romhack())).build());
        }
    }

    private static void generatePatchPage(CacheDatabase cacheDB) throws IOException {
        for (long id:cacheDB.getPatchById().keySet()) {
            Hack patch = cacheDB.getPatchById().get(id);
            TableBlock table = getRomhacksTable(cacheDB.getFilesByPatch().get(id));
            Path path = Path.of("../docs/database/patch/" + id + "/index");
            Files.createDirectories(path.getParent());
            write(path, 3, "Patch: " + patch.name(), table);
        }
    }

    private static void generateGamePage(CacheDatabase cacheDB) throws IOException {
        for (long id:cacheDB.getGameById().keySet()) {
            Game game = cacheDB.getGameById().get(id);
            TableBlock table = getRomhacksTable(cacheDB.getFilesByGame().get(id));
            Path path = Path.of("../docs/database/game/" + id + "/index");
            Files.createDirectories(path.getParent());
            write(path, 3, "Game: " + CSV.toString(cacheDB.parentFolderNamesById.get(game.id())), table);
        }
    }

    private static void generateSystemPage(CacheDatabase cacheDB) throws IOException {
        for (long id:cacheDB.getSystemById().keySet()) {
            System_ system = cacheDB.getSystemById().get(id);
            TableBlock table = getRomhacksTable(cacheDB.getFilesBySystem().get(id));
            Path path = Path.of("../docs/database/system/" + id + "/index");
            Files.createDirectories(path.getParent());
            write(path, 3, "System: " + CSV.toString(cacheDB.systemFolderNamesById.get(system.id())), table);
        }
    }

    private static void generateMainIndexPage(CacheDatabase cacheDB) throws IOException {
        List<TableBlock.TableRow> rows = new ArrayList<>();

        for (Long id:cacheDB.getSystemById().keySet()) {
            System_ system = cacheDB.getSystemById().get(id);
            List<String> rowValue = new ArrayList<>();
            UrlElement element = UrlElement.builder().tips(system.name()).url("system/" + id + "/index.md").build();
            rowValue.add(element.toMd());
            TableBlock.TableRow row = new TableBlock.TableRow();
            row.setRows(rowValue);
            rows.add(row);
        }

        TableBlock table = TableBlock.builder().titles(List.of("System")).rows(rows).build();
        Path path = Path.of("../docs/database/index");
        write(path, 1, "Systems:", table);
    }

}
