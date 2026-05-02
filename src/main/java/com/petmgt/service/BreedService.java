package com.petmgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetMapper;
import org.springframework.stereotype.Service;

@Service
public class BreedService {

    private final BreedMapper breedMapper;
    private final PetMapper petMapper;

    public BreedService(BreedMapper breedMapper, PetMapper petMapper) {
        this.breedMapper = breedMapper;
        this.petMapper = petMapper;
    }

    public Page<Breed> list(int pageNum, int pageSize, String petType) {
        Page<Breed> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Breed> wrapper = new LambdaQueryWrapper<>();
        if (petType != null && !petType.isEmpty()) {
            wrapper.eq(Breed::getPetType, petType);
        }
        wrapper.orderByDesc(Breed::getCreatedAt);
        return breedMapper.selectPage(page, wrapper);
    }

    public void save(Breed breed) {
        breedMapper.insert(breed);
    }

    public void update(Breed breed) {
        breedMapper.updateById(breed);
    }

    public void delete(Long id) {
        Long petCount = petMapper.selectCount(
            new LambdaQueryWrapper<Pet>().eq(Pet::getBreedId, id));
        if (petCount > 0) {
            throw new IllegalArgumentException("该品种下还有 " + petCount + " 只宠物，无法删除");
        }
        breedMapper.deleteById(id);
    }
}
