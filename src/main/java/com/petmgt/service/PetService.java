package com.petmgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.PetSearchCriteria;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetImageMapper;
import com.petmgt.mapper.PetMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PetService {

    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;

    public PetService(PetMapper petMapper, BreedMapper breedMapper, PetImageMapper petImageMapper) {
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
    }

    public Page<Pet> findPets(Page<Pet> page, PetSearchCriteria criteria) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        if (criteria.getPetType() != null && !criteria.getPetType().isEmpty()) {
            List<Breed> breeds = breedMapper.selectList(
                new LambdaQueryWrapper<Breed>().eq(Breed::getPetType, criteria.getPetType()));
            if (breeds.isEmpty()) {
                return page;
            }
            List<Long> breedIds = breeds.stream().map(Breed::getId).collect(Collectors.toList());
            wrapper.in(Pet::getBreedId, breedIds);
        }
        if (criteria.getBreedId() != null) {
            wrapper.eq(Pet::getBreedId, criteria.getBreedId());
        }
        if (criteria.getGender() != null && !criteria.getGender().isEmpty()) {
            wrapper.eq(Pet::getGender, criteria.getGender());
        }
        if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            wrapper.eq(Pet::getStatus, criteria.getStatus());
        }
        if (criteria.getName() != null && !criteria.getName().isEmpty()) {
            wrapper.like(Pet::getName, criteria.getName());
        }
        wrapper.orderByDesc(Pet::getCreatedAt);
        Page<Pet> result = petMapper.selectPage(page, wrapper);
        populateBreedNames(result.getRecords());
        populateCoverImages(result.getRecords());
        return result;
    }

    public Pet findPetDetail(Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            return null;
        }
        Breed breed = breedMapper.selectById(pet.getBreedId());
        if (breed != null) {
            pet.setBreedName(breed.getBreedName());
        }
        return pet;
    }

    public List<Pet> findLatestPets(int limit) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getStatus, "available")
               .orderByDesc(Pet::getCreatedAt)
               .last("LIMIT " + limit);
        List<Pet> pets = petMapper.selectList(wrapper);
        populateBreedNames(pets);
        populateCoverImages(pets);
        return pets;
    }

    public List<Pet> findAvailable() {
        List<Pet> pets = petMapper.selectList(
            new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, "available"));
        populateBreedNames(pets);
        populateCoverImages(pets);
        return pets;
    }

    public List<PetImage> findPetImages(Long petId) {
        return petImageMapper.selectList(
            new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, petId));
    }

    private void populateBreedNames(List<Pet> pets) {
        if (pets.isEmpty()) return;
        List<Long> breedIds = pets.stream().map(Pet::getBreedId).distinct().collect(Collectors.toList());
        List<Breed> breeds = breedMapper.selectBatchIds(breedIds);
        Map<Long, String> breedNameMap = breeds.stream()
            .collect(Collectors.toMap(Breed::getId, Breed::getBreedName));
        for (Pet pet : pets) {
            pet.setBreedName(breedNameMap.get(pet.getBreedId()));
        }
    }

    private void populateCoverImages(List<Pet> pets) {
        if (pets.isEmpty()) return;
        List<Long> petIds = pets.stream().map(Pet::getId).collect(Collectors.toList());
        List<PetImage> covers = petImageMapper.selectList(
            new LambdaQueryWrapper<PetImage>()
                .in(PetImage::getPetId, petIds)
                .eq(PetImage::getIsCover, 1));
        Map<Long, String> coverMap = covers.stream()
            .collect(Collectors.toMap(PetImage::getPetId, PetImage::getImageUrl, (a, b) -> a));
        for (Pet pet : pets) {
            pet.setCoverImageUrl(coverMap.get(pet.getId()));
        }
    }
}
