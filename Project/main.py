
from Simulator.World import World
import tkinter as tk
from tkinter import ttk

from Utils.GUI.GUI import GUI


def main():
    world = World(10, 10)
    world.populateWorld()
    world.simulate()
    world.printWorld()
    gui = GUI(world)
    gui.root.mainloop()


if __name__ == "__main__":
    main()
