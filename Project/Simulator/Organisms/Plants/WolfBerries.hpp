#pragma once

#include "@Plant.hpp"
class WolfBerries : public Plant {
   public:
    WolfBerries(World& world) : Plant(99, world, Type::WOLF_BERRIES) {}
    WolfBerries(nlohmann::json json, World& world) : Plant(json, world) {}
    void draw() override { std::cout << "🫐 "; }
    Plant* construct() const override { return new WolfBerries(world); }

    bool collisionReaction(Organism& other) override {
        other.Die();
        Die();
        return true;
    }
};
