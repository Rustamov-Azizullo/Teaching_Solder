package uz.askar.education.organization;

public final class OrganizationDtos {

    private OrganizationDtos() {
    }

    public record NamedRef(Long id, String name) {
    }

    public record MilitaryUnitDto(Long id, String name, Long militaryDistrictId, String militaryDistrictName) {

        public static MilitaryUnitDto from(MilitaryUnit unit) {
            return new MilitaryUnitDto(unit.getId(), unit.getName(),
                    unit.getMilitaryDistrict().getId(), unit.getMilitaryDistrict().getName());
        }
    }
}
