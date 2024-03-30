#include "Display.hpp"

#include <iostream>

#include "../Simulator/Organisms/Animals/Human.hpp"
#include "FileHandler.hpp"

using namespace std;

Display::Display(World &world) : world(world) {
    setlocale(LC_ALL, "");  // Set the locale to the default system locale
    initscr();              // Initialize the library
    start_color();          // Enable color
    use_default_colors();   // Use the terminal's default colors
    cbreak();               // Disable line buffering
    raw();                  // Disable line buffering
    keypad(stdscr, TRUE);   // Enable function and arrow keys
    noecho();               // Don't echo any keypresses
    curs_set(0);            // Hide the cursor

    const char *title = "Wojciech Siwiec s197815";
    mvwprintw(stdscr, 0, 0, title);
    refresh();

    left = newwin(LINES - 1, COLS / 2, 1, 0);
    topRight = newwin(LINES / 1.5 + .5, COLS / 2, 1, COLS / 2);
    bottomRight = newwin(LINES - (LINES / 1.5) - 1, COLS / 2,
                         (LINES + 1) / 1.5 + 1, COLS / 2);
}

Display::~Display() {
    clear();              // Clear the screen
    refresh();            // Refresh the screen to show the new state
    delwin(left);         // Delete the left window
    delwin(topRight);     // Delete the topRight window
    delwin(bottomRight);  // Delete the bottomRight window
    endwin();             // End the library
}

void Display::refreshWindows() const {
    box(left, 0, 0);
    box(topRight, 0, 0);
    box(bottomRight, 0, 0);

    wrefresh(left);
    wrefresh(topRight);
    wrefresh(bottomRight);
}

void Display::eraseWindows() const {
    werase(left);         // Clear the left window
    werase(topRight);     // Clear the topRight window
    werase(bottomRight);  // Clear the bottomRight window
}

void Display::menu(bool &endFlag) const {
    eraseWindows();

    bool exitFlag = false;

    while (!exitFlag) {
        bool shift = false;
        if (world.checkTime() > 0) {
            mvwprintw(left, 1, 1, "0. Continue the game");
            shift = true;
        }
        mvwprintw(left, 1 + shift, 1, "1. Start the game");
        mvwprintw(left, 2 + shift, 1, "2. Load the game");
        mvwprintw(left, 3 + shift, 1, "3. Save the game");
        mvwprintw(left, 4 + shift, 1, "4. Exit");

        refreshWindows();

        int ch = wgetch(left);

        switch (ch) {
            case '0':
                exitFlag = true;
                gameView();
                break;
            case '1':
                exitFlag = true;
                world.setWorld(world.getWidth(), world.getHeight(), 0);
                world.setOrganisms({});
                world.generateOrganisms();
                world.clearLogs();
                gameView();
                break;
            case '2': {
                FileHandler fileHandler("save.json");
                fileHandler.loadWorld(world);
            } break;
            case '3': {
                std::string fileName = getSaveFileName();
                FileHandler fileHandler(fileName);
                fileHandler.saveWorld(world);
            } break;
            case '4':
            case KEY_EXIT:
            case CTRL('c'):
            case 'q':
                exitFlag = true;
                endFlag = true;
                break;
            default:
                break;
        }
    }
}
void Display::gameView() const {
    eraseWindows();

    int max_y = 0, max_x = 0;
    getmaxyx(left, max_y, max_x);

    mvwprintw(bottomRight, 1, 1, "Time: %d", world.checkTime());
    mvwprintw(bottomRight, 2, 1, "Organisms: %d", world.getOrganisms().size());

    int shift_x = (max_x - world.getWidth() * 2) / 2;
    int shift_y = (max_y - world.getHeight()) / 2;
    if (shift_x < 0 || shift_y < 0) {
        mvwprintw(left, 1, 1, "Window too small");
        refreshWindows();
        getch();
        return;
    }

    for (size_t y = 0; y < (int)world.getHeight(); y++) {
        for (size_t x = 0; x < (int)world.getWidth(); x++) {
            Tile *tile = world.getTile(x, y);
            if (!tile->isFree()) {
                std::string str = tile->getOrganism()->getSymbol();
                std::wstring wstr(str.length(), L' ');
                std::mbstowcs(&wstr[0], str.c_str(), str.length());
                mvwaddwstr(left, y + shift_y, 2 * x + shift_x, wstr.c_str());
            } else {
                mvwaddwstr(left, y + shift_y, 2 * x + shift_x, L"🔳");
            }
        }
    }
    for (size_t i = 0; i < std::min(max_y, (int)world.getLogs().size()); i++) {
        mvwprintw(topRight, i + 1, 1, world.getLogs()[i].c_str());
    }
    refreshWindows();
}

std::string Display::getSaveFileName() const {
    eraseWindows();
    curs_set(1);

    int max_y = 0, max_x = 0;
    getmaxyx(left, max_y, max_x);

    mvwprintw(left, 1, 1, "Enter the name of the save file:");
    refreshWindows();

    char fileNameC[256];
    echo();
    mvwscanw(left, 2, 1, "%255s", fileNameC);
    noecho();
    string fileName = fileNameC + string(".json");

    curs_set(0);
    eraseWindows();
    return fileName;
}