package no.nav.folketrygdloven.kalkulator.modell.diff;


public class DiffForKopieringDto {

    private TraverseGraph traverser;

    public DiffForKopieringDto(TraverseGraph traverser) {
        this.traverser = traverser;
    }

    public <V> DiffForKopieringResult diff(V dto1, V dto2) {
        TraverseGraph.TraverseResult dto1Result = traverser.traverse(dto1);
        TraverseGraph.TraverseResult dto2Result = traverser.traverse(dto2);

        return new DiffForKopieringResult(this.traverser, dto1Result, dto2Result);
    }

    public <V> boolean areDifferent(V entity1, V entity2) {
        return !diff(entity1, entity2).getLeafDifferences().isEmpty();
    }

}
